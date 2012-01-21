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

public class ROMVersions {
	private String name;
	private List<ROMVersion> versions;
	private String phoneModel;
	private List<String> mirrorList;

	public ROMVersions() {
		name = "";
		phoneModel = "";
	}

	public String getName() {
		return name;
	}

	public void setName(String sName) {
		name = sName;
	}

	public List<ROMVersion> getVersions() {
		return versions;
	}

	public void setVersions(List<ROMVersion> ROMList) {
		versions = ROMList;
		try {
			boolean isEmpty = versions.isEmpty();
		} catch (Exception e) {
			versions = new ArrayList<ROMVersion>();
		}
	}

	public String getPhoneModel() {
		return phoneModel;
	}

	public List<String> getMirrorList() {
		return mirrorList;
	}
}
