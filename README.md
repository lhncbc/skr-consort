# CONSORT adherence (Work in progress)

## ULMFit training 

fastai libraries and python3 are required.

1. create train, validation, test data file from the dataset
- `cd ULMFit`
- `python data_creation_consort.py` 

Note: Copy `data_lm_consort.pkl` and `enc_best_consort.pth` to `ULMFit/data`. Change the train file name in `data_creation_consort.py` to train with a different file.


2. Finetune the language model and train a classification model based on the data file that is generated above
- `python finetune_consort.py`

Note:  The file `datasets/consort_golden_label_data.txt` contains sentences from all 50 annotated abstracts. Some file names are hardcoded in the scripts, so they may need to be changed.

## Phrase-based heuristics

`gov.nih.nlm.consort.pmc.ExtractionByPhrase`: assigns CONSORT items to Methods sentences using a simple phrase-based heuristics. It takes two arguments, XML input directory (`datasets/XML_50`) and the output file name (current output is in `phrase_heuristics_out.txt`). 

`gov.nih.nlm.consort.pmc.Evaluation`: evaluates the output against the gold annotations. It takes two arguments, the output file name (e.g., `phrase_heuristics_out.txt`) and the gold annotations (`datasets/gold_50.txt`).
 
 
## Contact

- Halil Kilicoglu:      [halil.kilicoglu@gmail.com](mailto:halil.kilicoglu@gmail.com)
