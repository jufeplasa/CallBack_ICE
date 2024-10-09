import com.zeroc.Ice.Current;
public class PrinterI implements Demo.Printer {

    @Override
    public String printString(String s, Current current) {
        System.out.println(s);
        return "";
    }

}