<?php
	include 'SpreadSheet.php';
	
	if (isset($_GET['time']) && isset($_GET['sender']) && isset($_GET['receiver']) && isset($_GET['line']))
	{
		$Spreadsheet = new Spreadsheet("graceplainsdoc@gmail.com", "remap2014");
		$Spreadsheet->setSpreadsheet("Log")->setWorksheet("gplog")
		->add(array("Time" => $_GET['time'], "Sender" => $_GET['sender'], "Receiver" => $_GET['receiver'], "Line" => $_GET['line']));
	}
?>