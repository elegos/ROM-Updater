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

import android.text.TextUtils;

public class ROMVersion {
	private String changelog;
	private String version;
	private String uri;
	
	public String getChangelog() {
		int pipe;
		String logs = "";
		String log = changelog;
		while((pipe = log.indexOf('|')) != -1) {
			logs += log.substring(0,pipe)+"\n";
			log = log.substring(pipe+1);
		}
		if(!TextUtils.isEmpty(log)) logs += log+"\n";
		return logs;
	}
	public void setChangelog(String c) {
		changelog = c;
	}
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String v) {
		version = v;
	}
	
	public String getUri() {
		return uri;
	}
	public void setUri(String u) {
		uri = u;
	}
}
