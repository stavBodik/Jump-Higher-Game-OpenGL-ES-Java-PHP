<?php
header("Content-Type: text/html; charset=utf-8");

		$email = $_GET['useremail'];

		

			require_once('dbConnect.php');
			mysqli_set_charset($con,"utf8");
			$sql = "SELECT * FROM users WHERE email='$email'";

			

			$check = mysqli_fetch_assoc(mysqli_query($con,$sql));

			if(isset($check)){

                                                // email found

                                                $string='1';

                                                foreach ($check as $value) {

                                                $string .= ','.$value; }



		                echo $string;



			}else{				 

                                                 echo '-1'; 

			}

			mysqli_close($con);

		

?>