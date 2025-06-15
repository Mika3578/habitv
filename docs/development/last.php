<?php
$directory="repository/com/dabi/habitv/habiTv";
$autoriseSnapshot=true;

function startsWith($haystack, $needle)
{
    return $needle === "" || strpos($haystack, $needle) === 0;
}
function endsWith($haystack, $needle)
{
    return $needle === "" || substr($haystack, -strlen($needle)) === $needle;
}

function checkPath($path)
{
    return $_GET['snapshot']=="true" || strpos($path,'SNAPSHOT') == false;
}

function checkFile($path)
{
    return strpos($path,"habiTv") !== false && endsWith($path,".jar");
}

function lastModifiedInFolder($folderPath) {

    /* First we set up the iterator */
    $iterator = new RecursiveDirectoryIterator($folderPath);
    $directoryIterator = new RecursiveIteratorIterator($iterator);

    /* Sets a var to receive the last modified filename */
    $lastModifiedFile = "";        

    /* Then we walk through all the files inside all folders in the base folder */
    foreach ($directoryIterator as $name => $object) {
        //echo $lastModifiedFile;
		/* In the first iteration, we set the $lastModified */
        if (empty($lastModifiedFile) && checkFile($name) && checkPath($name)) {
            $lastModifiedFile = $name;
        }
        else {
            $dateModifiedCandidate = filemtime($lastModifiedFile);
            $dateModifiedCurrent = filemtime($name);

            /* If the file we thought to be the last modified 
               was modified before the current one, then we set it to the current */
            if ($dateModifiedCandidate < $dateModifiedCurrent && checkFile($name) && checkPath($name)) {
                $lastModifiedFile = $name;
            }
        }
    }
    /* If the $lastModifiedFile isn't set, there were no files
       we throw an exception */
    if (empty($lastModifiedFile)) {
        //throw new Exception("No files in the directory");
    }

    return $lastModifiedFile;
}
//echo lastModifiedInFolder($directory);
header("Location: ".lastModifiedInFolder($directory));
die();
?>