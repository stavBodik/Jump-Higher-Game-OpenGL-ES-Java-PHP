<?php
	define('HOST','127.0.0.1');
	define('USER','scott');
	define('PASS','tiger');
	define('DB','u634119425_jumph');
	
	$con = mysqli_connect(HOST,USER,PASS,DB);
	if (!$con) {
		echo "Error: Unable to connect to MySQL." . PHP_EOL;
		echo "Debugging errno: " . mysqli_connect_errno() . PHP_EOL;
		echo "Debugging error: " . mysqli_connect_error() . PHP_EOL;
		exit;
	}
	
?>

