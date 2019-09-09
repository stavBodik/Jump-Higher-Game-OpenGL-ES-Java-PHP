<?php
header("Content-Type: text/html; charset=utf-8");

require_once('dbConnect.php');
mysqli_set_charset($con,"utf8");
$username = $_GET['username'];
$password = $_GET['password'];
 
$sql =" select * from users where username='$username' and password='$password' ";
 
$res = mysqli_query($con,$sql);
 
$check = mysqli_fetch_array($res);
 
$res=0;
if(isset($check)){
$res=1;
}

if($res=1){

$userid=$check[0];

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
WHERE username='$username' and password='$password'
";
 
$res1 = mysqli_query($con,$sql);
 
$check1 = mysqli_fetch_assoc($res1);

$string='1';
foreach ($check1 as $value) {
    $string .= ','.$value;
}

echo $string;


}else{
echo '0';
}
 
mysqli_close($con);
?>