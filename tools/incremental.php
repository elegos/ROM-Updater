#!/usr/bin/php
<?php
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

function loadDirectory($dir,$dir_name) {
	$array = array();
	
	if(is_dir($dir)) {
		if($dh = opendir($dir)) {
			while (($file = readdir($dh)) !== false) {
				if($file == "." || $file == "..") continue;
				if(is_dir("$dir/$file")) {
					$recursive = loadDirectory("$dir/$file",$dir_name);
					foreach($recursive as $rfile => $rhash)
						$array[$rfile] = $rhash;
				}
				else {
					$fname = substr("$dir/$file",strlen($dir_name)+1);
					$array[$fname] = md5_file("$dir/$file");
				}
			}
		}
	}
	return $array;
}

function copyFiles($array,$destRoot,$fromRoot) {
	foreach($array as $file) {
		$folder = substr($file,0,strrpos($file, "/"));
		@mkdir($destRoot."/".$folder, 0777, true);
		@unlink("$destRoot/$file");
		if(strpos($file,"updater-script") || strpos($file, "CERT.") || strpos($file, "MANIFEST.")) continue;
		echo "copying $destRoot/$file\n";
		copy("$fromRoot/$file", "$destRoot/$file");
	}
}

function createUpdaterScript($deleted, $destRoot) {
	echo "\nCreating updater-script file (CHECK AND MODIFY IT!)...\n";
	@mkdir($destRoot."/META-INF/com/google/android/");
	$fp = fopen($destRoot."/META-INF/com/google/android/updater-script","w");
	fwrite($fp,"ui_print(\"Incremental update - automatically created with the ROM Updater tool\");\n");
	fwrite($fp,"ui_print(\"Mounting /system, /data and /system/sd (as ext3) if mmcblk0p2 exists\");\n");
	fwrite($fp,"mount(\"yaffs2\", \"MTD\", \"system\", \"/system\");\n");
	fwrite($fp,"mount(\"yaffs2\", \"MTD\", \"userdata\", \"/data\");\n");
	fwrite($fp,"mount(\"ext3\", \"MCC\", \"/dev/block/mmcblk0p2\", \"/system/sd\");\n");

	fwrite($fp,"ui_print(\"Deleting old files...\");\n");
	foreach($deleted as $file) {
		fwrite($fp,"delete(\"/$file\")\n");
	}

	if(is_dir("$destRoot/system")) {
		fwrite($fp,"ui_print(\"Extracting /system files...\");\n");
		fwrite($fp,"package_extract_dir(\"system\", \"/system\");\n");
	}

	if(is_dir("$destRoot/data")) {
		fwrite($fp,"ui_print(\"Extracting /data files...\");\n");
		fwrite($fp,"package_extract_dir(\"data\", \"/data\");\n");
	}

	if(file_exists("$destRoot/boot.img")) {
		fwrite($fp,"ui_print(\"Installing new boot image...\");\n");
		fwrite($fp,"assert(package_extract_file(\"boot.img\", \"/tmp/boot.img\"), write_raw_image(\"/tmp/boot.img\", \"boot\"), delete(\"/tmp/boot.img\"));\");\n");
	}
	fwrite($fp,"unmount(\"/data\");\n");
	fwrite($fp,"unmount(\"/system/sd\");\n");
	fwrite($fp,"unmount(\"/system\");\n");
	fclose($fp);
}

$short_opt = "o:n:hw";
$long_opt = array("test","help");

$options = getopt($short_opt, $long_opt);

if(isset($options["h"]) || isset($options["help"])) {
	echo "EleGoS ROM Updater folders comparator\n";
	echo "Available parameters:\n";
	echo "\t-o\tfolder of the Old version (required)\n";
	echo "\t-n\tfolder of the New version (required)\n";
	echo "\t-w\tcreate a new folder with the changes (optional)\n";
	echo "\t-h\tSee --help\n";
	echo "\t--help\tShow this help\n";
	die();
}

if(!isset($options["n"]) || !isset($options["o"])) {
	die("\tUse --help to see the list of parameters.\n");
}

$v1 = loadDirectory($options["o"],$options["o"]);
$v2 = loadDirectory($options["n"],$options["n"]);

$removed = array();
$changed = array();
$new = array();

foreach($v1 as $file => $hash) {
	if(isset($v2[$file])) {
		if($hash != $v2[$file]) $changed[] = $file;
	} else {
		$removed[] = $file;
	}
}

foreach($v2 as $file => $hash) {
	if(!isset($v1[$file]))
		$new[] = $file;
}

echo "Files that were removed:\n";
foreach($removed as $file)
	echo "\t$file\n";
echo "\nFiles that were changed:\n";
foreach($changed as $file)
	echo "\t$file\n";
echo "\nFiles introduced in the new version:\n";
foreach($new as $file)
	echo "\t$file\n";

if(isset($options["w"])) {
	echo "\nCreating incremental structure (files will be overwritten)...\n";
	copyFiles($changed,"incremental",$options["n"]);
	copyFiles($new, "incremental",$options["n"]);
	copyFiles(array("META-INF/com/android/metadata","META-INF/com/google/android/update-binary"), "incremental",$options["n"]);
	createUpdaterScript($removed, "incremental");
}

echo "Done.\n";

?>