<template>
  <div class="info-data-radio">
    <el-radio-group ref="radioRef" v-model="state.sValue" v-bind="attrs" v-on="listeners">
      <component
        :is="radioElement"
        v-for="item in state.list"
        :key="item.value"
        :label="item.value"
        :disabled="item.disabled"
        :title="item.label"
      >
        {{ item.label }}
      </component>
    </el-radio-group>
  </div>
</template>
<script>
import { reactive, computed, watch, ref } from '@vue/composition-api';
import { isNil } from 'lodash';

export default {
  name: 'InfoRadio',
  inheritAttrs: false,
  model: {
    prop: 'value',
    event: 'change',
  },
  props: {
    request: Function, // 预留
    value: {
      type: [String, Number],
    },
    type: {
      type: String,
    },
    labelKey: {
      type: String,
      default: 'label',
    },
    valueKey: {
      type: String,
      default: 'value',
    },
    dataSource: {
      type: Array,
      default: () => [],
    },
    transformOptions: Function,
    innerRef: Function,
  },
  setup(props, ctx) {
    const { labelKey, valueKey, innerRef, transformOptions } = props;

    const elementRef = !isNil(innerRef) ? innerRef() : ref(null);

    const buildOptions = (list) =>
      list.map((d) => ({
        ...d,
        label: d[labelKey],
        value: d[valueKey],
      }));

    const rawList = buildOptions(props.dataSource);

    const list = typeof transformOptions === 'function' ? transformOptions(rawList) : rawList;

    const state = reactive({
      list,
      sValue: !isNil(props.value) ? props.value : undefined,
    });

    const handleChange = (value) => {
      ctx.emit('change', value);
    };

    watch(
      () => props.value,
      (next) => {
        Object.assign(state, {
          sValue: next,
        });
      }
    );

    watch(
      () => props.dataSource,
      (next) => {
        Object.assign(state, {
          list: buildOptions(next),
        });
      }
    );

    const radioElement = computed(() => (props.type === 'button' ? 'el-radio-button' : 'el-radio'));
    const attrs = computed(() => ctx.attrs);
    const listeners = computed(() => ({
      ...ctx.listeners,
      change: handleChange,
    }));

    return {
      state,
      attrs,
      elementRef,
      listeners,
      radioElement,
      handleChange,
    };
  },
};
</script>
