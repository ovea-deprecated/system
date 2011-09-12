def proc = 'C:\\cygwin\\bin\\ls.exe -al /cygdrive/d/kha/workspace/ovea/project/pipe/src'.execute() | 'C:\\cygwin\\bin\\cut.exe -c 50-'.execute() | 'C:\\cygwin\\bin\\grep.exe -v -E "^\\.\\.?$"'.execute()
proc.waitFor()
print proc.exitValue() ? proc.err.text : proc.text
