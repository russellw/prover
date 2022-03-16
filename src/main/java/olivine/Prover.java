package olivine;

public final class Prover
{
    private enum Language {
        DIMACS,
        TPTP,
    }

    private static String file;

    private static void setFile(String s){
        if(file!=null){
            System.err.printf("%s: file already specified\n",s);
            System.exit(1);
        }
        file=s;
    }

    private static void args(String[] args)  {
        for (var i = 0; i < args.length; i++) {
            var arg = args[i];
            if (arg.charAt(0)!='-'){
               setFile(arg);
               continue;
            }
        }
        }

    public static void main( String[] args )
    {
        args(args);
    }
}
