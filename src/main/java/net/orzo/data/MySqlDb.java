/*
 * Copyright (C) 2015 Tomas Machalek
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
package net.orzo.data;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class MySqlDb {

    public Database connect(final String connectionUri) throws SQLException {
        return new Database() {
            final Connection conn = DriverManager.getConnection(String.format("jdbc:%s", connectionUri));

            @Override
            public Iterator<Object[]> select(String query, Object...args) throws SQLException {
                PreparedStatement stmt = this.conn.prepareStatement(query);
                for (int i = 0; i < args.length; i++) {
                    stmt.setString(i + 1, args[i].toString());
                }
                stmt.execute();
                ResultSet res = stmt.getResultSet();
                ResultSetMetaData rsmd = res.getMetaData();
                int numCols = rsmd.getColumnCount();
                Object[] row;
                List<Object[]> ans = new ArrayList<>();
                while (res.next()) {
                    row = new Object[numCols];
                    for (int i = 1; i <= numCols; i++) {
                        row[i - 1] = res.getObject(i);
                    }
                    ans.add(row);
                }
                return ans.iterator();
            }

            private void setArg(int i, Object v, PreparedStatement stmt) throws SQLException {
                if (v instanceof String) {
                    stmt.setString(i, (String)v);

                } else if (v instanceof Integer) {
                    stmt.setInt(i, (Integer)v);

                } else if (v instanceof Double) {
                    stmt.setDouble(i, (Double)v);

                } else if (v instanceof jdk.nashorn.internal.runtime.Undefined) {
                    stmt.setNull(i, Types.VARCHAR); // TODO

                } else {
                    stmt.setObject(i, v); // TODO
                }
            }

            @Override
            public void modify(String query, Object...args) throws SQLException {
                PreparedStatement stmt = this.conn.prepareStatement(query);
                for (int i = 0; i < args.length; i++) {
                    this.setArg(i + 1, args[i], stmt);
                }
                stmt.execute();
            }

            public void close() throws SQLException {
                conn.close();
            }
        };
    }
}


