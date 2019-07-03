# To train model using ULMFit:

# Requirements:
	You need to install fastai libraries and python3

# 0. create train, valid, test data file from your dataset
	cd ULMFit
	python data_creation_consort.py
	(Need to copy ~Zeshan/consort/ULMFit/data/data_lm_consort.pkl & ~Zeshan/consort/ULMFit/data/enc_best_consort.pth to ULMFit/data/)
	(Change the train file name in data_creation_consort.py if you want to change your train file)

# 1. finetune the language model and train a classification model based on the data file that is generated above
	python finetune_consort.py

# Remember:
	You may need to change the filenames in the code to do different experiments


# The file "datasets/consort_golden_label_data.txt" contains sentences from all 50 annotated abstracts.
