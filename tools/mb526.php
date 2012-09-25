<?php

// _REQUEST vars are already urldecoded
$v = (isset($_REQUEST["v"])) ? $_REQUEST["v"]: '';
$f = (isset($_REQUEST["f"])) ? $_REQUEST["f"]: '';

if (!empty($f)) {
	header("Location: http://defy.wdscript.fr/defyplus-cm7/$f");
	exit;
} else {
	
	header("Content-Type: text/javascript");
	
	if (!empty($v)) {
		if (!strpos($v,"/")) {
?>
{
	"full": "<?=$v?>-Defy+.zip",
	"fromVersion": [
		{ "version":"7.1.0", "uri": "<?=$v?>-Defy+.zip" }
	]
}
<?
		}
	} else {
		echo "nothing here";
	}
}
?>
