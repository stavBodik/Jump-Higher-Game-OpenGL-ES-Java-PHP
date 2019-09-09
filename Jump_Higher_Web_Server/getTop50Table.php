<?php

$isMyRank = $_GET['ismyrank'];
$userRank=$_GET['userrank'];

$rowStart=0;
$howMuch=50;

if($isMyRank=="1"){
$rowStart=	intval($userRank)-25;

	   if($rowStart<=0){
		    $rowStart=0;
	   }
}

require_once('dbConnect.php');

$sql = "

SELECT users.id,rank,username,level,time,country
FROM (
SELECT id,rank, level, TIME
    FROM (
    SELECT @rank := @rank +1 AS rank, id, level, TIME
    FROM rank, (
    SELECT @rank :=0
    )r
    ORDER BY level DESC , TIME ASC
    )rankordered
)rankordered,users
where rankordered.id=users.id
ORDER BY rank ASC
limit $rowStart,$howMuch
";


                                			
$res = mysqli_query($con,$sql);

$post = array();
while($row = mysqli_fetch_assoc($res))
{
	$post[] = $row;
}

$string='';

foreach ($post as $row) 
{ 
	foreach ($row as $element)
	{
		
		$string .= ','.$element;
	}
	$string .= '$';
}

echo $string;

mysqli_close($con);
		
?>