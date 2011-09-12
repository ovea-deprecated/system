def exit = args[0] as int
if (exit > 0) {
    System.err.println('ERR:' + exit)
    System.in.close()
    System.out.close()
    System.err.close()
} else {
    System.in.withStream {InputStream is ->
        int c
        while ((c = is.read()) != -1) {
            System.out.write(c)
        }
        System.out.write((' ' + exit).bytes)
    }
}
System.exit(exit)
