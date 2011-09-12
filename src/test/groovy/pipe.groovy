/**
 * Copyright (C) 2011 Ovea <dev@ovea.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
def proc = 'C:\\cygwin\\bin\\ls.exe -al /cygdrive/d/kha/workspace/ovea/project/pipe/src'.execute() | 'C:\\cygwin\\bin\\cut.exe -cd 50-'.execute() | 'C:\\cygwin\\bin\\grep.exe -v -E "^\\.\\.?$"'.execute()
proc.waitFor()
print proc.exitValue() ? proc.err.text : proc.text
