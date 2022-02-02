<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Frameset//EN""http://www.w3.org/TR/REC-html40/frameset.dtd">
<html>
<head>
<link rel="stylesheet" href="help.css" type="text/css" media="screen" />
</head>

<script language="javascript" type="text/javascript">

window.onload = function windowLoad(event)
{
   	var arrElements = document.getElementsByTagName("a");
      
   	for (var i=0; i<arrElements.length; i++)
	{
   		var element=arrElements[i];
   		
   		if (element.className == "example")
		{
   			element.onclick = function() { parent.appletFrame.document.miscApplet.runCode(this.text); };
   			element.href = "#";
   		}
	}
}

</script>

<body>
<center>
<div id=body>
<div id="title"><?php
$page = $_GET['page'];
$contents = fopen("help-contents.txt", "r");

$helpfile = split("\n", fread($contents, filesize("help-contents.txt")));
$count = sizeof($helpfile);
$helpfile = $helpfile[(int)($page)];

$fp = fopen("$helpfile.html", "r");
$help = split("\n", fread($fp, filesize("$helpfile.html")), 2);

?><span class=nav><?php if ($page > 0) {echo '<a href="help.php?page='; echo $page-1; echo '">&lt;&lt; last</a>';}?>&nbsp;&nbsp;<?php if ($page+1 < $count) {echo '<a href="help.php?page='; echo $page+1; echo '">next &gt;&gt;</a>';}

echo "</span>";
echo $_GET['page'];
echo " : ";
echo $help[0];

?></div>
<?php echo $help[1];?>
</div>
</body>
</html>