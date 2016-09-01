#!/bin/bash

#---- Usage : ScriptName QueryLogfile App Database
#---- Convert QueryLogfile to Row/Column Cordinates
#---- Sanjay Ganvkar, 12-Aug-2016, First-Cut

FILE=$1
APP=$2
DB=$3

OUTPUTDIR="output"
FILENAME="`basename "${FILE}" ".qlg"`"
TMPFILE="mx"
GRIDFILE=${OUTPUTDIR}/${FILENAME}.txt

mkdir ${OUTPUTDIR} 2>/dev/null
rm ${TMPFILE}* 2>/dev/null
rm ${GRIDFILE}* 2>/dev/null

#------------- Example of Partial Sample from QueryLog conversion to grid-cordinate file -----------------
# <subquery>
# <cluster size="9">
# <dim size="3"><member>Jan]<member>Feb]<member>Mar]</dim>
# <dim size="1"><member>Sales]</dim>
# <dim size="3"><member>100]<member>200]<member>400]</dim>
# <dim size="1"><member>East]</dim>
# <dim size="1"><member>Actual]</dim>
# <dim size="1"><member>Years]</dim>
# </cluster>
# </subquery>
#
# ..... to
#
#  +   +  [Sales] [East] [Actual] [Years]
# Jan	100
# Jan 200 
# ........ to a grid file ( via awk )
# Sales,0,2
# East  0,3
# Jan   1,0
# Jan   2.0
#
# ----------------------------------------------------------------------------------------------------------

# Split the QueryLog in multiple files , each containing a sub-query
csplit -s ${FILE} '/^<subquery>$/' '{*}' -f ${TMPFILE}

echo ""
echo "Started converting file ${FILE} at `date` ...."

# For all files containing a grid call
for fil in `grep -l "dim size" ${TMPFILE}*`
do
	(
	# e.g Convert <dim size="3"><member>Jan]<member>Feb]<member>Mar]</dim> 
	#    to [Jan],[Feb],[Mar]..
	# e.g Convert <dim size="1"><member>Actual]</dim>
	#    to [Actual]
	# And feed both varieties to the awk script
	isStr="`grep "dim s" ${fil}|grep -v '<dim size="1">'`"
	if [ "${isStr}" != "" ]
	then
		grep "dim s"  ${fil} | sed -e "s/^.dim size=.[0-9]*\">//g" -e "s/<\/dim>$//g" -e  "s/<member>/[/g" -e  "s/<\/member>/]/g" -e "s/\]\[/],[/g"
	fi
	) | sed -e "s/^.dim size=.[0-9]*\">//g" -e "s/<\/dim>$//g" -e  "s/<member>/[/g" -e  "s/<\/member>/]/g" -e "s/\]\[/],[/g"  |awk -f log2grid2.awk |sed -e "s/\[//g" -e "s/\]//g"  >> ${GRIDFILE}
done
rm ${TMPFILE}*
echo "Ended   converting file ${FILE} at `date`"
echo ""
echo "GRID output can be found in ${GRIDFILE}"
echo "-----------------------------------------------------------"
