<?php
header("Content-Type: text/html; charset=utf-8");

		$username = $_GET['username'];
                                //$username = utf8_decode($username);
		$password = $_GET['password'];
		$email = $_GET['email'];
                                $country = $_GET['country'];

		
			require_once('dbConnect.php');
                                                mysqli_set_charset($con,"utf8");
                                               // $con->set_charset("utf8");
			$sql = "SELECT * FROM users WHERE username='$username' OR email='$email'";
			
			$check = mysqli_fetch_array(mysqli_query($con,$sql));
			
			if(isset($check)){
				echo '-1';
			}else{				
				$sql = "INSERT INTO users (username,password,email,country) VALUES('$username','$password','$email','$country')";
                                                                mysqli_set_charset($con,"utf8");
				if(mysqli_query($con,$sql)){
                                                                               $userid=mysqli_insert_id($con);
                                                                               $sql1 = "INSERT INTO rank (id,time,level) VALUES($userid,'59:59:59','1')";
                                                                               mysqli_query($con,$sql1);

					echo $userid;
				}else{
					echo '0';
				}
			}
			mysqli_close($con);
		
?>