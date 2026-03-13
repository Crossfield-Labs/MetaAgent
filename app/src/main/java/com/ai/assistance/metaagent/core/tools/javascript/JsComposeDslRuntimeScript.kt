package com.ai.assistance.metaagent.core.tools.javascript

internal fun buildComposeDslRuntimeWrappedScript(script: String): String {
    return """
        $script

        (function() {
            function __metaagent_is_promise(__value) {
                return !!(__value && typeof __value.then === 'function');
            }

            function __metaagent_wrap_compose_response(__bundle, __tree) {
                return {
                    tree: __tree,
                    state: __bundle.state,
                    memo: __bundle.memo
                };
            }

            function __metaagent_build_compose_response(__bundle, __entry) {
                var __tree = __entry(__bundle.ctx);
                if (__metaagent_is_promise(__tree)) {
                    return __tree.then(function(__resolvedTree) {
                        return __metaagent_wrap_compose_response(__bundle, __resolvedTree);
                    });
                }
                return __metaagent_wrap_compose_response(__bundle, __tree);
            }

            function __metaagentResolveComposeEntry() {
                try {
                    if (typeof module !== 'undefined' && module && module.exports) {
                        if (typeof module.exports.default === 'function') {
                            return module.exports.default;
                        }
                        if (typeof module.exports.Screen === 'function') {
                            return module.exports.Screen;
                        }
                    }
                    if (typeof exports !== 'undefined' && exports) {
                        if (typeof exports.default === 'function') {
                            return exports.default;
                        }
                        if (typeof exports.Screen === 'function') {
                            return exports.Screen;
                        }
                    }
                    if (typeof window !== 'undefined') {
                        if (typeof window.default === 'function') {
                            return window.default;
                        }
                        if (typeof window.Screen === 'function') {
                            return window.Screen;
                        }
                    }
                } catch (e) {
                    console.error('resolve compose entry failed:', e);
                }
                return null;
            }

            function __metaagent_render_compose_dsl(__runtimeOptions) {
                if (typeof MetaAgentComposeDslRuntime === 'undefined') {
                    throw new Error('MetaAgentComposeDslRuntime bridge is not initialized');
                }
                var __root = typeof globalThis !== 'undefined'
                    ? globalThis
                    : (typeof window !== 'undefined' ? window : this);
                var __activeCallRuntime =
                    typeof __root.__metaagent_call_runtime_ref === 'object' && __root.__metaagent_call_runtime_ref
                        ? __root.__metaagent_call_runtime_ref
                        : null;
                var __options = __runtimeOptions && typeof __runtimeOptions === 'object'
                    ? Object.assign({}, __runtimeOptions)
                    : {};
                if (__activeCallRuntime) {
                    __options.__metaagent_call_runtime = __activeCallRuntime;
                }
                var __bundle = MetaAgentComposeDslRuntime.createContext(__options);
                var __entry = __metaagentResolveComposeEntry();
                if (typeof __entry !== 'function') {
                    throw new Error(
                        'compose_dsl entry function not found, expected default export or Screen function'
                    );
                }
                if (__activeCallRuntime && typeof __bundle.setCallRuntime === 'function') {
                    __bundle.setCallRuntime(__activeCallRuntime);
                }
                __root.__metaagent_compose_bundle = __bundle;
                __root.__metaagent_compose_entry = __entry;
                return __metaagent_build_compose_response(__bundle, __entry);
            }

            function __metaagent_dispatch_compose_dsl_action(__actionRequest) {
                var __root = typeof globalThis !== 'undefined'
                    ? globalThis
                    : (typeof window !== 'undefined' ? window : this);
                var __bundle = __root.__metaagent_compose_bundle;
                var __entry = __root.__metaagent_compose_entry;
                if (!__bundle || typeof __entry !== 'function') {
                    throw new Error('compose_dsl runtime is not initialized, render first');
                }
                if (typeof __bundle.invokeAction !== 'function') {
                    throw new Error('compose_dsl runtime action bridge is not available');
                }
                var __activeCallRuntime =
                    typeof __root.__metaagent_call_runtime_ref === 'object' && __root.__metaagent_call_runtime_ref
                        ? __root.__metaagent_call_runtime_ref
                        : null;
                if (__activeCallRuntime && typeof __bundle.setCallRuntime === 'function') {
                    __bundle.setCallRuntime(__activeCallRuntime);
                }

                var __request =
                    __actionRequest && typeof __actionRequest === 'object'
                        ? __actionRequest
                        : {};
                var __actionId = String(
                    __request.__action_id || __request.actionId || ''
                ).trim();
                if (!__actionId) {
                    throw new Error('compose action id is required');
                }

                var __payload =
                    Object.prototype.hasOwnProperty.call(__request, '__action_payload')
                        ? __request.__action_payload
                        : __request.payload;
                var __noRender =
                    __payload &&
                    typeof __payload === 'object' &&
                    (__payload.__no_render === true ||
                        __payload.__noRender === true ||
                        __payload.__local === true);

                function __metaagent_send_intermediate_result(__value) {
                    if (typeof sendIntermediateResult !== 'function') {
                        return;
                    }
                    sendIntermediateResult(__value);
                }

                var __actionSettled = false;
                var __intermediateRenderQueued = false;
                var __intermediateRenderInFlight = false;
                var __unsubscribeStateChange = null;

                function __metaagent_finalize_action() {
                    __actionSettled = true;
                    if (typeof __unsubscribeStateChange === 'function') {
                        try {
                            __unsubscribeStateChange();
                        } catch (__unsubscribeError) {
                        }
                        __unsubscribeStateChange = null;
                    }
                }

                function __metaagent_render_and_send_intermediate() {
                    if (__actionSettled) {
                        return null;
                    }
                    try {
                        var __intermediateResponse = __metaagent_build_compose_response(__bundle, __entry);
                        if (__metaagent_is_promise(__intermediateResponse)) {
                            return __intermediateResponse.then(function(__resolvedIntermediate) {
                                if (!__actionSettled) {
                                    __metaagent_send_intermediate_result(__resolvedIntermediate);
                                }
                            });
                        }
                        __metaagent_send_intermediate_result(__intermediateResponse);
                    } catch (__intermediateError) {
                        try {
                            console.warn('compose intermediate render failed:', __intermediateError);
                        } catch (__ignore) {
                        }
                    }
                    return null;
                }

                function __metaagent_process_intermediate_queue() {
                    if (__actionSettled || __intermediateRenderInFlight || !__intermediateRenderQueued) {
                        return;
                    }
                    __intermediateRenderQueued = false;
                    __intermediateRenderInFlight = true;
                    var __renderResult = __metaagent_render_and_send_intermediate();
                    if (__metaagent_is_promise(__renderResult)) {
                        __renderResult.then(
                            function() {},
                            function() {}
                        ).then(function() {
                            __intermediateRenderInFlight = false;
                            if (__intermediateRenderQueued && !__actionSettled) {
                                __metaagent_process_intermediate_queue();
                            }
                        });
                        return;
                    }
                    __intermediateRenderInFlight = false;
                    if (__intermediateRenderQueued && !__actionSettled) {
                        __metaagent_process_intermediate_queue();
                    }
                }

                function __metaagent_schedule_intermediate_render() {
                    if (__actionSettled) {
                        return;
                    }
                    __intermediateRenderQueued = true;
                    Promise.resolve().then(function() {
                        __metaagent_process_intermediate_queue();
                    });
                }

                if (typeof __bundle.subscribeStateChange === 'function') {
                    if (!__noRender) {
                        __unsubscribeStateChange = __bundle.subscribeStateChange(function() {
                            __metaagent_schedule_intermediate_render();
                        });
                    }
                }

                var __maybePromise;
                try {
                    __maybePromise = __bundle.invokeAction(__actionId, __payload);
                } catch (__actionError) {
                    __metaagent_finalize_action();
                    throw __actionError;
                }
                if (__maybePromise && typeof __maybePromise.then === 'function') {
                    if (!__noRender) {
                        // For async actions, schedule a render checkpoint immediately.
                        // Additional state updates during await phases are pushed by state-change listeners.
                        __metaagent_schedule_intermediate_render();
                    }
                    return __maybePromise.then(function() {
                        __metaagent_finalize_action();
                        if (__noRender) {
                            return null;
                        }
                        return __metaagent_build_compose_response(__bundle, __entry);
                    }, function(__actionError) {
                        __metaagent_finalize_action();
                        throw __actionError;
                    });
                }
                __metaagent_finalize_action();
                if (__noRender) {
                    return null;
                }
                return __metaagent_build_compose_response(__bundle, __entry);
            }

            if (typeof exports !== 'undefined' && exports) {
                exports.__metaagent_render_compose_dsl = __metaagent_render_compose_dsl;
                exports.__metaagent_dispatch_compose_dsl_action =
                    __metaagent_dispatch_compose_dsl_action;
            }
            if (typeof module !== 'undefined' && module && module.exports) {
                module.exports.__metaagent_render_compose_dsl = __metaagent_render_compose_dsl;
                module.exports.__metaagent_dispatch_compose_dsl_action =
                    __metaagent_dispatch_compose_dsl_action;
            }
            var __root = typeof globalThis !== 'undefined'
                ? globalThis
                : (typeof window !== 'undefined' ? window : this);
            __root.__metaagent_render_compose_dsl = __metaagent_render_compose_dsl;
            __root.__metaagent_dispatch_compose_dsl_action =
                __metaagent_dispatch_compose_dsl_action;
        })();
    """.trimIndent()
}

