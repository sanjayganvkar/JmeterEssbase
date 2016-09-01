package dataquery;

/**
 * Application will read the grid-coordinates files and submit grid-api requests to Essbase
 * Parameters are
 * UserName Password providerUrl servername app db gridfilename
 * @author gsanjay
 */
import com.essbase.api.base.*;
import com.essbase.api.session.*;
import com.essbase.api.dataquery.*;
import com.essbase.api.domain.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;

import java.util.ArrayList;


public class DataQuery {

    private static String s_userName = "";
    private static String s_password = "";
    private static String s_provider = "";
    private static String s_analyticSvrName = "";
    private static String s_appName = "";
    private static String s_cubeName = "";
    private static String s_gridfileName = "";
    private static String s_printOutput = "";

    private static final int FAILURE_CODE = 1;

    public static void main(String[] args) {

        s_userName = args[0];
        s_password = args[1];
        s_provider = args[2];
        s_analyticSvrName = args[3];
        s_appName = args[4];
        s_cubeName = args[5];
        s_gridfileName = args[6];
        
        if ( args[7] != null )
        {
            s_printOutput = args[7];
        }
        

        int statusCode = 0;
        IEssbase ess = null;
        IEssCubeView cv = null;

        BufferedReader br = null;
        String fstr = "";
        boolean first = true;
        int grid_counter = 0;

        try {
           // acceptArgs(args);

            // Create JAPI instance.
            ess = IEssbase.Home.create(IEssbase.JAPI_VERSION);

            // Sign on to the provider, and perform data query operations.
            IEssDomain dom = ess.signOn(s_userName, s_password, false, null, s_provider);
            cv = dom.openCubeView("Data Query Example", s_analyticSvrName, s_appName,
                    s_cubeName);

            // Set couple of cube view properties.
            cv.setRepeatMemberNames(false);
            cv.setIncludeSelection(true);
           
            cv.setSuppressMissing(true);
            cv.updatePropertyValues();
            // Read file and perform retrieve for each bunch of 
            // While no more GRIDSIZE 
            ArrayList<String> gridFileContents = new ArrayList<String>();

            br = new BufferedReader(new FileReader(s_gridfileName));
            while ((fstr = br.readLine()) != null) {
                gridFileContents.add(fstr);
            }
            br.close();

            ArrayList<String> request = new ArrayList<String>();
            for (String str : gridFileContents) {
                if (str.contains("GRIDSIZE") && first) {
                    first = false;
                    request.add(str);
                } else if (str.contains("GRIDSIZE") && !first) {
                    performCubeViewOperation(ess, cv, "retrieve", request);
                    System.out.println("Grid COunter " + grid_counter);
                    grid_counter++;
                    request = new ArrayList<String>();
                    request.add(str);
                } else {
                    request.add(str);
                }
            }
            performCubeViewOperation(ess, cv, "retrieve", request);

        } catch (Exception x) {
            System.err.println("ERROR: " + x.getMessage());
            statusCode = FAILURE_CODE;
        } finally {
            // Close cube view.
            try {
                if (cv != null) {
                    cv.close();
                }
            } catch (EssException x) {
                System.err.println("Error: " + x.getMessage());
            }

            // Sign off from the domain.
            try {
                if (ess != null && ess.isSignedOn() == true) {
                    ess.signOff();
                }
            } catch (EssException x) {
                System.err.println("Error: " + x.getMessage());
            }
        }
        // Set status to failure only if exception occurs and do abnormal termination
        // otherwise, it will by default terminate normally
        if (statusCode == FAILURE_CODE) {
            System.exit(FAILURE_CODE);
        }
    }

    static IEssGridView fillGrid(IEssCubeView cv, IEssGridView grid, ArrayList<String> request) {

        int i = 0;
        int row = 0;
        int col = 0;
        String dim = "";

        try {

            for (String str : request) {

                StringTokenizer st2 = new StringTokenizer(str, ",");
                i = 0;
                while (st2.hasMoreElements()) {

                    str = (String) st2.nextElement();

                    switch (i) {
                        case 0:
                            row = Integer.valueOf(str);
                            break;
                        case 1:
                            col = Integer.valueOf(str);
                            break;
                        case 2:
                            dim = str;
                            break;
                    }
                    i++;
                }

                //  System.out.println(row + " " + col + " " + dim);
                grid = cv.getGridView();

                if (dim.equals("GRIDSIZE")) {
                    grid.setSize(row, col);
                } else {

                    grid.setValue(row, col, dim);

                }

            }

        } catch (Exception e) {
            //for ( String str : request ) 
            // { 
            System.out.println("Empty Grid. Ignoring ...");
            //  };
            //e.printStackTrace();
        }
        return grid;
    }

    static void performCubeViewOperation(IEssbase ess, IEssCubeView cv,
            String opStr, ArrayList<String> request) throws EssException {
        // Create a grid view with the input for the operation.
        IEssGridView grid = cv.getGridView();

        grid = fillGrid(cv, grid, request);

        // Create the operation specification.
        IEssOperation op = null;
        op = cv.createIEssOpRetrieve();

        // Perform the operation.
        cv.performOperation(op);

        // Get the result and print the output.
        if (s_printOutput.equalsIgnoreCase("Y")) {
            int cntRows = grid.getCountRows(), cntCols = grid.getCountColumns();
            System.out.print("Query Results for the Operation: " + opStr + "\n"
                    + "-----------------------------------------------------\n");
            for (int i = 0; i < cntRows; i++) {
                for (int j = 0; j < cntCols; j++) {
                    System.out.print(grid.getValue(i, j) + "\t");
                }
                System.out.println();
            }
            System.out.println("\n");
        }
    }

}
