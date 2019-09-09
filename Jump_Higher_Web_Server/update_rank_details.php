<?php

$userid = $_GET['userid'];
$bestusertime = $_GET['bestusertime'];
$bestuserlevel = $_GET['bestuserlevel'];


require_once('dbConnect.php');
$sql = "SELECT * FROM users WHERE id='$userid'";
	
// check if userid exist                                          			
$check = mysqli_fetch_array(mysqli_query($con,$sql));


if(isset($check)){
// if user id found updated his rank
    $sql = "REPLACE INTO rank (id,time,level) VALUES($userid,'$bestusertime','$bestuserlevel')";
	if(mysqli_query($con,$sql)){
	// if succed get user all NEW info includes user rank  + total # of registarted users on server
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
}
else{
echo '-1';	
}

mysqli_close($con);
		
?>