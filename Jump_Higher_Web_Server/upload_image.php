<?php

    // Get image string posted from Android App

    $base=$_REQUEST['image'];

    // Get file name posted from Android App

    $filename = $_REQUEST['filename'];

    // Decode Image

    $binary=base64_decode($base);

    header('Content-Type: bitmap; charset=utf-8');

    // Images will be saved under 'www/imgupload/uplodedimages' folder

    $coo=getcwd();
     //$coo=chdir('public_html');
    $file = fopen($coo.'/UserProfilImages/big/'.$filename, 'wb');

    // Create File

    fwrite($file, $binary);

    fclose($file);

	resize(25,$coo.'/UserProfilImages/small/'.$filename,$coo.'/UserProfilImages/big/'.$filename);
	
    echo 'Image upload complete, Please check your php file directory';
	
	
	function resize($newWidth, $targetFile, $originalFile) {

			$info = getimagesize($originalFile);
			$mime = $info['mime'];

			switch ($mime) {

					case 'image/png':
							$image_create_func = 'imagecreatefrompng';
							$image_save_func = 'imagepng';
							$new_image_ext = 'png';
							break;

					

					default: 
							throw Exception('Unknown image type.');
			}

			$img = $image_create_func($originalFile);
			list($width, $height) = getimagesize($originalFile);

			$newHeight = ($height / $width) * $newWidth;
			$tmp = imagecreatetruecolor($newWidth, $newHeight);
			imagecopyresampled($tmp, $img, 0, 0, 0, 0, $newWidth, $newHeight, $width, $height);

			if (file_exists($targetFile)) {
					unlink($targetFile);
			}
			$image_save_func($tmp, "$targetFile");
	}

?>