import sys

def calculate(textFile, skip=0):
	total = 0
	count = 0
	with open(textFile, "r") as fd:
		# skip the first few lines because the way the file is formatted and 
		# because the first few replies are slow due to the server/peers startup late
		# we allow peer to all start up and start counting 
		lines = fd.readlines()[skip:]

		for line in lines:
			vals = line.split(" ")
			if (vals[0] != "\n"):
				total += int(vals[0])
				count += 1

	return (total / count)



if __name__ == "__main__":
	print(calculate(sys.argv[1], int(sys.argv[2])))