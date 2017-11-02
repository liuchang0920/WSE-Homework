package nyu.edu.wse.hw.domain;

public class QueryItem {

    private String query;
    private int ft;
    private int fdt;

    public QueryItem(String query, int ft, int fdt) {
        this.query = query;
        this.ft = ft;
        this.fdt = fdt;
    }

    public String getQuery() {
        return query;
    }

    public int getFt() {
        return ft;
    }

    public int getFdt() {
        return fdt;
    }

    @Override
    public String toString() {
        return "QueryItem{" +
                "query='" + query + '\'' +
                ", ft=" + ft +
                ", fdt=" + fdt +
                '}';
    }
}
