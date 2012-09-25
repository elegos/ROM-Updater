<?php
header("Content-Type: text/javascript");

$agent = @ "".$_SERVER["HTTP_USER_AGENT"];

$device = "MB525";
if (isset($_REQUEST["p"])) {
        $device = $_REQUEST["p"];
}

$JSON = new stdClass;
$JSON->name = "CyanogenMod";
$JSON->phoneModel = $device;
$JSON->versions = array();
$JSON->mirrorList = array();

if (strpos($agent,"MB525") !== false || (strpos($agent,"Apache-HttpClient") !== false && $device == "MB525")) {

        $JSON->versions = json_decode( <<<EOF
        [
          {"version":"72-20120304-NIGHTLY","changelog":"New stable Recovery and Bootmenu 1.1.6 with 2nd system choice"}
        , {"version":"72-20120601-NIGHTLY","changelog":"Boosted governor and sio scheduler, bootmenu 1.3.0, recovery keyrepeat, busybox 1.20.1" }
        ]
EOF
        );

        // common version json
        foreach ($JSON->versions as $v) {
                $v->uri = "http://".$_SERVER['SERVER_NAME']."/romupdater/mb525.php?v=";
        }

        echo json_encode($JSON)."\n";
        @ file_put_contents("main.json", json_encode($JSON));

} elseif (strpos($agent,"MB526") !== false || strpos($agent,"Apache-HttpClient") !== false) {

        // you can create a db or list files from your server here...

        $JSON->versions = json_decode( <<<EOF
        [
          { "version":"cm72-120127.0846-NIGHTLY", "changelog":"Recovery and bootmenu, Final rotate animations, Rom updater" }
        , { "version":"CM72-20120627-NIGHTLY", "changelog":"Post CM 7.2 (gingerbread branch) by Epsylon3 release for GB kernels with touch bootmenu and new fs kernel modules" }
        ]
EOF
        );

        // common version json
        foreach ($JSON->versions as $v) {
                $v->uri = "http://".$_SERVER['SERVER_NAME']."/romupdater/mb526.php?v=";
        }

        echo json_encode($JSON)."\n";

        // cache generated file, debug purpose...
        @ file_put_contents("MB526.json", json_encode($JSON));

} else {
        echo "nothing here for you $agent\n";
}
?>
