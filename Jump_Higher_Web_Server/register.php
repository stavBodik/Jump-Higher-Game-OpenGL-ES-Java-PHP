<?php

		$name = $_GET['name'];
		$username = $_GET['username'];
		$password = $_GET['password'];
		$email = $_GET['email'];
                                $country = $_GET['country'];

		
			require_once('../dbConnect.php');
			$sql = "SELECT * FROM usrs WHERE username='$username' OR email='$email'";
			
			$check = mysqli_fetch_array(mysqli_query($con,$sql));
			
			if(isset($check)){
				echo '-1';
			}else{				
				$sql = "INSERT INTO users (username,password,email,country) VALUES(	'$username','$password','$email','$country')";
				if(mysqli_query($con,$sql)){
					echo mysqli_insert_id($con);
				}else{
				    //echo("Error description: " . mysqli_error($con));
					echo '0';
				}
			}
			mysqli_close($con);
		
?>

