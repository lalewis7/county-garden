package src;
import java.sql.*;
import java.util.Scanner;

public interface TableItem {

    public void getContents(Scanner scan);

    public void getContents(ResultSet res) throws SQLException;

    public void printContents(Connection conn) throws SQLException;

    public void getNewID(Connection conn) throws SQLException;

}
