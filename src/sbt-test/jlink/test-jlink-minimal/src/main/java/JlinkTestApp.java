class JlinkTestApp {

  public static void main(String[] args) {
    // The corresponding module should have been linked since we're referring
    // to the class directly.
    try {
      System.out.println(javax.xml.XMLConstants.class);
      System.err.println("Directly referenced class should be present: OK");
    } catch (NoClassDefFoundError e) {
      System.err.println("Directly referenced class should be present: FAIL");
      System.exit(1);
    }

    try {
      // The corresponding module should not have been linked.
      System.out.println(Class.forName("java.util.logging.Logger"));
      System.err.println("Indirectly referenced class should not be present: FAIL");
      System.exit(1);
    } catch (ClassNotFoundException e) {
      System.err.println("Indirectly referenced class should not be present: OK");
    }
  }
}
