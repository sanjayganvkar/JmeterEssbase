# -----------------------------------------------------------
#---- Awk script to convert incoming lines into a dimensional array with grid coordinates
#---- Sanjay Ganvkar, 12-Aug-2016, First-Cut

# Convert input lines
# 
# [Sales] 
# [East]
# [Jan],[Feb]
# [100][200]

# [which can be visualized in a grid as below]

#  +   +  [Sales] [East]
# Jan	100
# Jan 200 
# Feb	100
# Feb 200 

# to

# ........ to a grid file , which acts as the input to the java app
# Sales,0,2
# East  0,3
# Jan   1,0
# Jan   2,0
# 100   1,1
# 100   3,1
# -----------------------------------------------------------

BEGIN {
	totelements=1
	lines=0
	FS=","
	headerelements=0
}
{
	split($0,a,",");
	x = length(a);
	if ( x == 1 )
	{
		for ( i in a)
		{
			header[t++] = a[i]
			++headerelements
		}
	}
	else
	{
		totelements *= NF
		details[lines]=$0
		++lines
	}
}
END {
	grid=totelements
	printf("%s,%s,%s\n",grid+1,lines+headerelements,"[GRIDSIZE]");

	for ( h in header )
	{
		printf("%s,%s,%s\n",0,h+lines,header[h]);
	}
	for ( i = 0; i < lines; i++ )
	{
		split(details[i],s,",")
		elements=length(s);
		repeat=totelements/elements
		rownum=1

		m=0
		while ( m < grid )
		{
			for ( el=1;el<=elements;el++)
			{
				for ( j = 0; j < repeat; j++ )
				{
				
					printf("%s,%s,%s\n", rownum++, i, s[el]);
					m++
				}
			}
			totelements=repeat
		}
	}

}
