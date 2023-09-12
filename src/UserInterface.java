package src;
import java.util.*;
import java.sql.*;

public abstract class UserInterface {
    
    protected Scanner scan;
    protected Connection conn;

    public UserInterface(Scanner scanner, Connection conn) {
        this.scan = scanner;
        this.conn = conn;
    }

    public abstract void start();

}
