<?php

$userid = $_GET['userid'];

require_once('dbConnect.php');
$sql = "SELECT * FROM rank WHERE id='$userid'";
	
// check if userid exist                                          			
$check = mysqli_fetch_array(mysqli_query($con,$sql));

if(isset($check)){
// if user id found send his information back to client

	$sql =
	"
	SELECT id,username,password,email,country,isLogedIN,rank,level,time,totalusers
	FROM (
	SELECT rank, level, TIME
	FROM (
	SELECT @rank := @rank +1 AS rank, id, level, TIME
	FROM rank, (
	SELECT @rank :=0
	)r
	ORDER BY level DESC , TIME ASC
	)t
	WHERE id = '$userid'
	)userrank
	JOIN users JOIN
	(
	SELECT COUNT( * ) AS totalusers
	FROM users
	)j
	WHERE id='$userid' 
	";
	
	
	$res1 = mysqli_query($con,$sql);
 
    $check1 = mysqli_fetch_assoc($res1);

	$string='1';
	foreach ($check1 as $value) {
		$string .= ','.$value;
	}

    echo $string;

}else{
echo '-1';
}

mysqli_close($con);
		
?>