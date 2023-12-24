package frontend.Symbol;


public class VarSymbol extends Symbol {

    private int con; // 1:const 0:var
    private int dimension; // the dimension of array, 0:a  1:a[]  2:a[][]
    public VarSymbol(String name, int con, int dimension) {
        super(name);
        this.con = con;
        this.dimension = dimension;
    }

    public int getCon() {
        return con;
    }

    public int getDimension() {
        return dimension;
    }

}
