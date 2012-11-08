##//billboard=(objectClass=dexels.apachecon.billboard.Billboard)
for i in 1..25
	$billboard.show("Some message: #{i}")
	$billboard.sleep(100)
end
return "Result #{i} iterations"

 
