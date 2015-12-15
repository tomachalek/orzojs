/*
 * Copyright (c) 2015 Tomas Machalek
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

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.*;
import net.orzo.SharedService;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

/**
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class MaxmindGeolocation implements SharedService<Ip2Geo> {

    private DatabaseReader geoip2Db;

    private final String dbPath;

    public MaxmindGeolocation(String dbPath) {
        this.dbPath = dbPath;
    }


    public Ip2Geo exportApi() throws IOException {
        File dbFile = new File(this.dbPath);
        this.geoip2Db = new DatabaseReader.Builder(dbFile).build();

        return (String ipString) -> {
            InetAddress ipAddress;
            GeoData ans = new GeoData();
            try {
                ipAddress = InetAddress.getByName(ipString);
                CityResponse response = MaxmindGeolocation.this.geoip2Db.city(ipAddress);
                Country country = response.getCountry();
                ans.countryISO = country.getIsoCode();
                ans.countryName = country.getName();

                Subdivision subdivision = response.getMostSpecificSubdivision();
                ans.subdivisionName = subdivision.getName();
                ans.subdivisionISO = subdivision.getIsoCode();

                City city = response.getCity();
                ans.cityName = city.getName();

                ans.postalCode = response.getPostal().getCode();

                Location location = response.getLocation();
                ans.latitude = location.getLatitude();
                ans.longitude = location.getLongitude();

            } catch (GeoIp2Exception | IOException e) {
                e.printStackTrace(); // TODO
                return ans;
            }

            return ans;
        };
    }

}
