from fastai.text import *
import sklearn.metrics

label_string = 'all_labels'
avg = 'macro'
data_dir = 'data/all_agree_filtered'
path = Path(data_dir)

# 1: THIS PART IS FOR GENERATING DATABUNCH FOR LANGUAGE MODEL AND CLASSIFICATION

#data_lm = TextLMDataBunch.from_csv(path, 'texts.csv', header=None, text_cols=0)
#data_lm.save('data_lm_consort.pkl')

data_lm = load_data(path, fname='../data_lm_consort.pkl')
train_df = pd.read_csv(path/'train.csv', header=None)
valid_df = pd.read_csv(path/'valid.csv', header=None)
test_df = pd.read_csv(path/'test.csv', header=None)
data_clas = TextClasDataBunch.from_df(path, train_df=train_df, valid_df=valid_df, test_df=test_df, vocab=data_lm.train_ds.vocab, bs=16, label_cols=0, text_cols=1)
data_clas.save('data_clas_all_agree_filtered.pkl')

# 2: THIS PART IS FOR FINE-TUNING PRETRAINED LANGUAGE MODEL

#data_lm = load_data(path, fname='data_lm_consort.pkl')
#data_clas = load_data(path, fname='data_clas_struc_abs_with_context.pkl', bs=40)

#learn = language_model_learner(data_lm, AWD_LSTM, drop_mult=0.5, pretrained_fnames=['lstm_wt103', 'itos_wt103'], pretrained=False)
#learn.fit_one_cycle(1, 1e-2)
#learn.unfreeze()
#learn.fit_one_cycle(10, 1e-3)
#learn.validate()
#learn.save_encoder('enc_best_consort')
#learn.save('lm_best_consort', with_opt=False)

# 3: THIS PART IS FOR BUILDING CLASSIFIER

data_clas = load_data(path, fname='data_clas_all_agree_filtered.pkl', bs=16)

learn = text_classifier_learner(data_clas, AWD_LSTM, drop_mult=0.5)
learn.load_encoder('../../enc_best_consort')
learn.freeze()
learn.fit_one_cycle(1)
learn.freeze_to(-2)
learn.fit_one_cycle(1)
learn.freeze_to(-3)
learn.fit_one_cycle(1)
learn.unfreeze()
best_acc = 0
for i in range(10):
    learn.fit_one_cycle(1)
    val = learn.validate()
    if(float(val[1]) > best_acc):
        best_acc = float(val[1])
        learn.save('best_cls_all_agree_filtered')
        print("Better accuracy_score: " + str(best_acc))

# 4: PREDICT RAW TEXT
data_clas = load_data(path, fname='data_clas_all_agree_filtered.pkl', bs=16)
learn = text_classifier_learner(data_clas, AWD_LSTM, drop_mult=0.5)
learn.load('best_cls_all_agree_filtered')
test_df = pd.read_csv(path / 'test.csv', header=None)
preds = []
targets = []
# out = open('Results/single_sentence_labels/' + label_string + '_results_single_sentence.txt', 'w')
for index, row in test_df.iterrows():
    target = int(row[0])
    targets.append(target)
    pred = int(learn.predict(row[1])[0])
    preds.append(pred)
    #print(str(target) + " " + str(pred) + "\n")
#     out.write(str(target) + '|' + str(pred) + '|' + row[1] + '\n')
# out.close()

preds = np.array(preds)
targets = np.array(targets)
f1s = sklearn.metrics.f1_score(targets, preds, average=None)
precisions = sklearn.metrics.precision_score(targets, preds, average=None)
recalls = sklearn.metrics.recall_score(targets, preds, average=None)
for i in range(len(f1s)):
    print("%d: f1: %.2f pre: %.2f re: %.2f" % (i, f1s[i], precisions[i], recalls[i]))

f1 = sklearn.metrics.f1_score(targets, preds, average=avg)
precision = sklearn.metrics.precision_score(targets, preds, average=avg)
recall = sklearn.metrics.recall_score(targets, preds, average=avg)
acc = sklearn.metrics.accuracy_score(targets, preds)
print("Overall: f1: %.2f pre: %.2f re: %.2f" % (f1, precision, recall))
# print(preds[1].tolist())
# print(preds[1].tolist().index(1))
# pred = learn.predict('Assessments during part 1 (randomised phase) of the study were performed at day 1 and at weeks 1, 2, 4, 8, 12, 16, 20 and 24.')
# print(pred)
