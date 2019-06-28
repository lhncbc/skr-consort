Here is the procedure for training model using fasttext:

0. Create validation and test dataset from reconciled files.
	- I have a java program("PMCSentenceExtraction.java") to parse those reconciled XML files(Only "METHODS" section) to |-seperated txt files that will have the same format as our training files.
		- it requires three input arguments: 1. the path to the directory that contains all reconciled files 2. the path to the output file that will contain sentences labels information. 3. the path to the output file that will contain all reconciled files IDs



1. preprocess train, validation and test dataset into fasttext processing data format 
	- (numpy and sklearn are needed to run this script)
	- python preprocess.py [path to the data directory]("datasets/")
	- Fortunately, there are three files generated after running this script. All have postfix of ".preprocessed" for train, validation and test dataset.


2. train fasttext model with different hyperparameters to find the best combination of hyperparameters
	- (numpy, sklearn, pandas are required)
	- (modify search grids if you want, and make sure enlarge the terminal window to print out everything)
	- python finetune_hyperparameters.py [path to the data directory]("consort_datasets")
	- the results are sorted by F1 scores


3. train fasttext model with the best combination of hyperparameters
	- fastText-0.1.0/fasttext supervised -input consort_datasets/train.txt.preprocessed -output fasttext-model -dim [best_dim] -wordNgrams [best_wordNgrams] -epoch [best_epoch] -verbose 1
	- this generates a model "fasttext-model" in the same directory


4. test trained fasttext model on test dataset
	- python test_fasttext_model.py [model_name]("fasttext-model") [path to the test file]("consort_datasets/test.txt.preprocessed")
	- this prints out F1 scores, precisions and recalls for each label and overall

To train model using ULMFit:

0. Use ULMFit/data_creation_consort.py to create the train.csv, valid.csv and test.csv from your dataset

1. Use ULMFit/finetune_consort.py to finetune the language model and train a classification model based on the dataset that is generated above (remember you may need to change some of the filenames in finetune_consort.py program to manipulate your dataset)



To calculate MASI, there are two ways:
	1. Use the Java file which will calculate the MASI value for the data in the xlsx file, e.g. "masi.xlsx"
	2. Use the php file
		- Command: php calcMASI.php file1 file2 
			this will calculate the MASI value for two annotated XML files


The file "datasets/consort_golden_label_data.txt" contains sentences from all 50 annotated abstracts.
