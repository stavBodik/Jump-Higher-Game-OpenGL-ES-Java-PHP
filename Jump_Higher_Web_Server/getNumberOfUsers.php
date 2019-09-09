<?php

require_once('dbConnect.php');
$sql = "SELECT count(*) FROM users";

$res = mysqli_query($con,$sql);
 
$check = mysqli_fetch_array($res);
 
$res=0;
if(isset($check)){
$res=1;
}

if($res=1){
	//echo mysqli_num_rows($res); 
	echo $check[0];
}else{
	echo '0';
}

mysqli_close($con);
		
?>