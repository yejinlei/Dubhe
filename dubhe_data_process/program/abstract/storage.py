# !/usr/bin/env python
# -*- coding:utf-8 -*-

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
=============================================================
"""

from abc import ABCMeta
from abc import abstractmethod


class Storage(metaclass=ABCMeta):
    """
    algorithm task storage
    """

    @abstractmethod
    def init_client(self):
        """
        init method
        """
        pass

    @abstractmethod
    def get_one_task(*args):
        """
        Get a task
        Parameter description:
        args[0]: Lua expression
        args[1]: numkeys default 1
        args[2]: Pending task queue
        args[3]: Task queue in process
        args[4]: time
        """
        pass

    @abstractmethod
    def save_result(*args):
        """
        Save the results
        """
        pass
