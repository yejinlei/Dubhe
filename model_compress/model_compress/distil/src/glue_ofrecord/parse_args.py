import argparse


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        '--data_dir',
        help='Input glue task directories.',
        default=None,
        type=str,
        required=True
    )
    parser.add_argument(
        '--output_dir',
        help='Output the directory of oneflow record files.',
        default=None,
        type=str,
        required=True
    )
    parser.add_argument(
        '--vocab_file',
        help='The vocabulary file that the BERT model was trained on.',
        default=None,
        type=str,
        required=True
    )
    parser.add_argument(
        '--do_lower_case',
        help='Whether to lower case the input text. Should be True for uncased '
             'models and False for cased models.',
        default=None,
        type=bool
    )
    parser.add_argument(
        '--max_seq_length',
        help='Maximum sequence length.',
        default=128,
        type=int
    )
    parser.add_argument(
        '--do_train',
        help='Whether to process the training data',
        default=None,
        type=bool
    )
    parser.add_argument(
        '--do_eval',
        help='Whether to process the validation data',
        default=None,
        type=bool
    )
    parser.add_argument(
        '--do_predict',
        help='Whether to process the prediction data',
        default=None,
        type=bool
    )
    parser.add_argument(
        '--aug_train',
        help='Whether to process the augmented training data',
        default=None,
        type=bool
    )
    parser.add_argument(
        '--task_name',
        help='The task of glue to be processed',
        default='cola',
        type=str
    )

    return parser.parse_args()
