/*
 * This file is part of ROMUpdater.

 * ROMUpdater is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * ROMUpdater is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with ROMUpdater.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.elegosproject.romupdater.types;

import java.util.ArrayList;
import java.util.List;

public class AvailableVersions {
	private String full;
	private List<AvailableVersion> fromVersion;

	public String getFullUri() {
		return full;
	}
	
	public void setFullUri(String u) {
		full = u;
	}
	
	public List<AvailableVersion> getAvailableVersions() {
		return fromVersion;
	}

	public void setAvailableVersions(List<AvailableVersion> VersionsList) {
		fromVersion = VersionsList;
		try {
			boolean isEmpty = fromVersion.isEmpty();
		} catch (Exception e) {
			fromVersion = new ArrayList<AvailableVersion>();
		}
	}
	
	public String getVersionFileName(String version) {
		for(AvailableVersion av : getAvailableVersions()) {
			if(av.getVersion() == version)
				return av.getUri();
		}
		return "";
	}
}
