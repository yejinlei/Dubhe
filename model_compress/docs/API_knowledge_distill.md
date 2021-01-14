知识蒸馏
=========

"软标签蒸馏"算子: pred_distill
---------

 `knowledge_distill_util.pred_distill(args, student_logits, teacher_logits):`

[源代码](../model_compress/distil/src/knowledge_distill_util.py#L381)

`pred_distill`为teacher和student模型添加软标签损失，使得student模型可以学习教师模型的输出，达到student模型模仿teacher模型在预测层的表现的目的。
采用[soft_cross_entropy](../model_compress/distil/src/knowledge_distill_util.py#L336)来计算损失。

**参数：**

- **args**： 一些超参，如teacher_temperature和student_temperature，对student和teacher模型进行soft操作的温度值。
- **student_logits**： student模型预测出的logits。
- **teacher_logits**： teacher模型预测出的logits。


**返回：** 由teacher模型和student模型组合得到的软标签损失。

---
"层与层蒸馏"算子: layer_distill
---------

`knowledge_distill_util.layer_distill(args, student_reps, teacher_reps):`

[源代码](../model_compress/distil/src/knowledge_distill_util.py#L346)

`layer_distill`为teacher和student模型添加层与层损失，使得student模型可以学习教师模型的隐藏层特征，达到用teacher模型的暗知识（Dark Knowledge）指导student模型学习的目的，将teacher模型中的知识更好的蒸馏到student模型中。通过[MSE](../model_compress/distil/src/knowledge_distill_util.py#L343)来计算student模型和teacher模型中间层的距离。

**参数：**

- **args**： 一些超参，暂未用到，仅留出接口。
- **student_reps**： student模型的所有中间层表示。
- **teacher_reps**： teacher模型的所有中间层表示。


**返回：** 由teacher模型和student模型组合得到的层与层蒸馏损失。

>注：该算子仅适用于BERT类的student和teacher模型。

---
"注意力蒸馏"算子: att_distill
---------

`knowledge_distill_util.att_distill(args, student_atts, teacher_atts):`

[源代码](../model_compress/distil/src/knowledge_distill_util.py#L363)

`att_distill`为teacher和student模型添加注意力损失，使得student模型可以学习教师模型的attention score矩阵，学习到其中包含语义知识，例如语法和相互关系等。通过[MSE](../model_compress/distil/src/knowledge_distill_util.py#L343)来计算损失。

**参数：**

- **args**： 一些超参，暂未用到，仅留出接口。
- **student_reps**： student模型的所有的attention score矩阵。
- **teacher_reps**： teacher模型的所有的attention score矩阵。


**返回：** 由teacher模型和student模型组合得到的注意力蒸馏损失。

>注：该算子仅适用于BERT类的student和teacher模型。
