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
import org.hyperic.sigar.Sigar
import com.ovea.system.util.SigarLoader
import org.hyperic.sigar.NetFlags
import org.hyperic.sigar.NetConnection

Sigar sigar = SigarLoader.instance()
NetConnection[] connections = sigar.getNetConnectionList( NetFlags.CONN_CLIENT | NetFlags.CONN_PROTOCOLS)
println connections
connections.each {NetConnection con ->
    println "${con.type} ${con.localPort} ${sigar.getProcPort(con.type, con.localPort)}"
}