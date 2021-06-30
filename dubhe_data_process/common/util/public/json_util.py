# _*_ coding:utf-8 _*_
import json


class JsonUtil:

    def __init__(self):
        pass

    # noinspection PyMethodMayBeStatic
    def load_json(self):
        """
        read json file
        """
        with open(self, encoding="utf-8") as f:
            json_object = json.loads(f.read())
        return json_object
