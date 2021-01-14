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
import json
import oneflow as flow

# mysingle.py
class ModelWeights:
    weights_dict={}
    dtype_dict={flow.float32:2,
                flow.float64:3,
                flow.int8:4,
                flow.int32:5,
                flow.int64:6,
                flow.float16:9,
                2:2, 3:3, 4:4, 5:5, 6:6, 9:9}
    
    def add(self, variable_name, dtype, shape):
        assert variable_name not in self.weights_dict                
        profile_dict={}
        profile_dict["dtype"]=dtype
        profile_dict["shape"]=shape
        self.weights_dict[variable_name]=profile_dict       
        return self.weights_dict
    
    def addConv(self, index, dtype, shape1, shape2, optimizer):
        dtype = self.dtype_dict[dtype]
#        print(dtype)
        self.add("conv{}".format(index)+'_weight', dtype, shape1)
        self.add("conv{}".format(index)+'_bias', dtype, shape2)
        self.add("conv{}".format(index)+'_bn-gamma', dtype, shape2)
        self.add("conv{}".format(index)+'_bn-beta', dtype, shape2)
        self.add("conv{}".format(index)+'_bn-moving_variance', dtype, shape2)
        self.add("conv{}".format(index)+'_bn-moving_mean', dtype, shape2)
        if optimizer == 'adam':
            self.add("conv{}".format(index)+'_weight-v', dtype, shape1)
            self.add("conv{}".format(index)+'_weight-m', dtype, shape1)
            self.add("conv{}".format(index)+'_bias-v', dtype, shape2)
            self.add("conv{}".format(index)+'_bias-m', dtype, shape2)
            self.add("conv{}".format(index)+'_bn-gamma-v', dtype, shape2)
            self.add("conv{}".format(index)+'_bn-gamma-m', dtype, shape2)
            self.add("conv{}".format(index)+'_bn-beta-v', dtype, shape2)
            self.add("conv{}".format(index)+'_bn-beta-m', dtype, shape2)
        elif optimizer == 'momentum':
            self.add("conv{}".format(index)+'_weight-momentum', dtype, shape1)
            self.add("conv{}".format(index)+'_bias-momentum', dtype, shape2)
            self.add("conv{}".format(index)+'_bn-gamma-momentum', dtype, shape2)
            self.add("conv{}".format(index)+'_bn-beta-momentum', dtype, shape2)
    
    def addDense(self, dtype_old, shape, optimizer, dense_num):
        dtype = []
        for old in dtype_old:
            dtype.append(self.dtype_dict[old])
#        print(dtype)
        for i in range(0, dense_num):
            self.add('dense'+str(i)+'-weight', dtype[i], shape[i])
            self.add('dense'+str(i)+'-bias', dtype[i], (shape[i][0],))
            if optimizer == 'adam':
                self.add('dense'+str(i)+'-weight-v', dtype[i], shape[i])
                self.add('dense'+str(i)+'-weight-m', dtype[i], shape[i])
                self.add('dense'+str(i)+'-bias-v', dtype[i], (shape[i][0],))
                self.add('dense'+str(i)+'-bias-m', dtype[i], (shape[i][0],))
            elif optimizer == 'momentum':
                self.add('dense'+str(i)+'-weight-momentum', dtype[i], shape[i])
                self.add('dense'+str(i)+'-bias-momentum', dtype[i], (shape[i][0],))

            
    def save(self,path):
        print('Saving weights_profile_path to {}'.format(path))
#        print('weights_dict',self.weights_dict)
        with open(path,"w") as f:
            for k,v in self.weights_dict.items():
                v_json=json.dumps(v)
                f.write(k+'__'+ v_json +'\n')
        return self.weights_dict

    def load(self,path):
        if len(self.weights_dict)!=0:
            return self.weights_dict
        else:
            with open(path,'r') as f:
                for line in f:
                    variable_name,profile_dict=line.split('__') 
                    profile_dict=json.loads(profile_dict)
                    self.weights_dict[variable_name]=profile_dict
            return self.weights_dict

modelWeight = ModelWeights()