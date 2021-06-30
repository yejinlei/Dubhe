"""
Copyright 2020 Tianshu AI Platform. All Rights Reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
"""
import re
import inspect
# from ac_opf.models import ac_model


class FindClassInFile:
    """
    find class in the given module

    ### note ###
    There is only one class in the given module.
    If there are more than one classes, all of them will be omit except the first
    ############


    method: find
        args:
            module: object-> the given file or module
            encoding: string-> "utf8" by default
        output: tuple(string-> name of the class, object-> class)


    usage:

        # >>> import module
        #
        # >>> find_class_in_file = FindClassInFile()
        # >>> cls = find_class_in_file.find(module)
        #
        # >>> cls_instance = cls[1](args)

    """

    def __init__(self):
        pass

    def _open_file(self, path, encoding="utf8"):
        with open(path, "r", encoding=encoding) as f:
            data = f.readlines()

        for line in data:
            yield line

    def find(self, module, encoding="utf8"):
        path = module.__file__
        lines = self._open_file(path=path, encoding=encoding)

        cls = ""
        for line in lines:
            if "class " in line:
                cls = re.findall("class (.*?)[:(]", line)[0]
            if cls:
                break

        return self._valid(module, cls)

    def _valid(self, module, cls):
        members = inspect.getmembers(module)
        cand = [(i, j) for i, j in members if inspect.isclass(j) and (not inspect.isabstract(j)) and (i == cls)]
        if not cand:
            print("class not found in {}".format(module))
        return cand[0]


if __name__ == "__main__":
    find_class_in_file = FindClassInFile()
    # cls = find_class_in_file.find(ac_model)

    # print(cls)







