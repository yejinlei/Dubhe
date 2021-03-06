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

import abc, sys
import torch
from tqdm import tqdm
from kamal.core import metrics
from kamal.utils import set_mode
from typing import Any, Callable
from .engine import Engine
from .events import DefaultEvents
from kamal.core import callbacks

import weakref
from kamal.utils import move_to_device, split_batch

class BasicEvaluator(Engine):
    def __init__(self,
                 dataloader: torch.utils.data.DataLoader,
                 metric: metrics.MetricCompose,
                 eval_fn: Callable=None,
                 tag: str='Eval',
                 progress: bool=False ):
        super( BasicEvaluator, self ).__init__()
        self.dataloader = dataloader
        self.metric = metric
        self.progress = progress
        if progress:
            self.porgress_callback = self.add_callback( 
                DefaultEvents.AFTER_STEP, callbacks=callbacks.ProgressCallback(max_iter=len(self.dataloader), tag=tag))
        self._model = None
        self._tag = tag
        if eval_fn is None:
            eval_fn = BasicEvaluator.default_eval_fn
        self.eval_fn = eval_fn

    def eval(self, model, device=None):
        device = device if device is not None else \
            torch.device( 'cuda' if torch.cuda.is_available() else 'cpu' )
        self._model = weakref.ref(model) # use weakref here
        self.device = device
        self.metric.reset()
        model.to(device)
        if self.progress:
            self.porgress_callback.callback.reset()
        with torch.no_grad(), set_mode(model, training=False):
            super(BasicEvaluator, self).run( self.step_fn, self.dataloader, max_iter=len(self.dataloader) )
        return self.metric.get_results()
    
    @property
    def model(self):
        if self._model is not None:
            return self._model()
        return None

    def step_fn(self, engine, batch):
        batch = move_to_device(batch, self.device)
        self.eval_fn( engine, batch )
        
    @staticmethod
    def default_eval_fn(evaluator, batch):
        model = evaluator.model
        inputs, targets = split_batch(batch)
        outputs = model( inputs )
        evaluator.metric.update( outputs, targets )
        
    
class TeacherEvaluator(BasicEvaluator):
    def __init__(self,
                 dataloader: torch.utils.data.DataLoader,
                 teacher: torch.nn.Module,
                 task,
                 metric: metrics.MetricCompose,
                 eval_fn: Callable=None,
                 tag: str='Eval',
                 progress: bool=False ):
        if eval_fn is None:
            eval_fn = TeacherEvaluator.default_eval_fn
        super( TeacherEvaluator, self ).__init__(dataloader=dataloader, metric=metric, eval_fn=eval_fn, tag=tag, progress=progress)
        self._teacher = teacher
        self.task = task
    
    def eval(self, model, device=None):
        self.teacher.to(device)
        with set_mode(self.teacher, training=False):
            return super(TeacherEvaluator, self).eval( model, device=device )

    @property
    def model(self):
        if self._model is not None:
            return self._model()
        return None

    @property
    def teacher(self):
        return self._teacher

    def step_fn(self, engine, batch):
        batch = move_to_device(batch, self.device)
        self.eval_fn( engine, batch )
        
    @staticmethod
    def default_eval_fn(evaluator, batch):
        model = evaluator.model
        teacher = evaluator.teacher

        inputs, targets = split_batch(batch)
        outputs = model( inputs )

        # get teacher outputs
        if isinstance(teacher, torch.nn.ModuleList):
            targets = [ task.predict(tea(inputs)) for (tea, task) in zip(teacher, evaluator.task)  ]
        else:
            t_outputs = teacher(inputs)
            targets = evaluator.task.predict( t_outputs )
        evaluator.metric.update( outputs, targets )