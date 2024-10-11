module Demo
{
    interface Callback{
        void reportResponse(string response);
    }

    interface Printer
    {
        void printString(string s, Callback* callback);
    }
}