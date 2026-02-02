/*
METADATA
{
    // 学习通智能助手包
    name: Xuexitong_Smart_Assistant
    description: { zh: "学习通智能助手，通过UI自动化技术实现学习通应用交互，支持课程浏览、资料提取、OCR识别和智能问答。自动整理课程资料并存入记忆库，让AI能基于课程内容回答问题。", en: "Xuexitong smart assistant powered by UI automation. Supports course browsing, material extraction, OCR recognition, and intelligent Q&A based on course content." }

    // Tools in this package
    tools: [
        {
            name: workflow_guide
            description: { zh: "学习通助手工具使用流程指南。完成学习资料整理的典型流程：\\n1. **打开学习通**: 使用 `open_xuexitong` 启动学习通App。\\n2. **选择课程**: 使用 `select_course` 选择要学习的课程。\\n3. **浏览章节**: 使用 `browse_chapters` 查看课程章节列表。\\n4. **提取资料**: 使用 `extract_chapter_content` 提取章节内容（包括文本和图片OCR）。\\n5. **保存到记忆库**: 使用 `save_to_memory` 将提取的内容保存到记忆库。\\n6. **智能问答**: 之后可以直接问AI关于课程的问题，AI会从记忆库中检索相关内容回答。", en: "Xuexitong assistant workflow:\\n1. Open app with `open_xuexitong`\\n2. Select course with `select_course`\\n3. Browse chapters with `browse_chapters`\\n4. Extract content with `extract_chapter_content` (text + OCR)\\n5. Save to memory with `save_to_memory`\\n6. Ask AI questions based on the stored content." }
            parameters: []
        },
        {
            name: open_xuexitong
            description: { zh: "打开学习通App", en: "Open Xuexitong app." }
            parameters: []
        },
        {
            name: select_course
            description: { zh: "选择指定的课程", en: "Select a specific course." }
            parameters: [
                {
                    name: course_name
                    description: { zh: "课程名称关键词", en: "Course name keyword." }
                    type: string
                    required: true
                }
            ]
        },
        {
            name: browse_chapters
            description: { zh: "浏览当前课程的章节列表", en: "Browse chapter list of current course." }
            parameters: []
        },
        {
            name: extract_chapter_content
            description: { zh: "提取指定章节的内容，包括文本识别和图片OCR", en: "Extract chapter content including text and image OCR." }
            parameters: [
                {
                    name: chapter_name
                    description: { zh: "章节名称关键词", en: "Chapter name keyword." }
                    type: string
                    required: true
                }
            ]
        },
        {
            name: save_to_memory
            description: { zh: "将提取的课程内容保存到记忆库", en: "Save extracted content to memory library." }
            parameters: [
                {
                    name: course_name
                    description: { zh: "课程名称", en: "Course name." }
                    type: string
                    required: true
                },
                {
                    name: chapter_name
                    description: { zh: "章节名称", en: "Chapter name." }
                    type: string
                    required: true
                },
                {
                    name: content
                    description: { zh: "章节内容", en: "Chapter content." }
                    type: string
                    required: true
                }
            ]
        },
        {
            name: navigate_to_home
            description: { zh: "返回学习通首页", en: "Navigate to Xuexitong home page." }
            parameters: []
        }
    ]
}
*/

const XuexitongAssistant = (function () {

    // 学习通App包名
    const XUEXITONG_PACKAGE = "com.chaoxing.mobile";

    // Helper to create response objects
    function createResponse(success: boolean, message: string, data: any = {}) {
        return { success, message, ...data };
    }

    /**
     * 打开学习通App
     */
    async function open_xuexitong(): Promise<any> {
        try {
            console.log("正在启动学习通App...");

            // 启动学习通
            await Tools.System.startApp(XUEXITONG_PACKAGE);
            await Tools.System.sleep(3000);

            return createResponse(true, "学习通已打开");
        } catch (e: any) {
            return createResponse(false, "打开学习通失败: " + e.message);
        }
    }

    /**
     * 选择课程
     */
    async function select_course(params: { course_name: string }): Promise<any> {
        try {
            const courseName = params.course_name;
            console.log(`正在查找课程: ${courseName}`);

            // 获取当前页面
            const page = await UINode.getCurrentPage();

            // 查找课程
            const courseElement = page.findByText(courseName);
            if (!courseElement) {
                return createResponse(false, "未找到课程: " + courseName);
            }

            // 点击课程
            await courseElement.click();
            await Tools.System.sleep(2000);

            return createResponse(true, "已进入课程: " + courseName);
        } catch (e: any) {
            return createResponse(false, "选择课程失败: " + e.message);
        }
    }

    /**
     * 浏览章节列表
     */
    async function browse_chapters(): Promise<any> {
        try {
            console.log("正在浏览章节列表...");

            // 获取当前页面
            const page = await UINode.getCurrentPage();
            const pageText = page.getText();

            // 提取章节信息
            const chapters: string[] = [];
            const lines = pageText.split('\n');

            for (const line of lines) {
                const trimmed = line.trim();
                // 简单的章节识别：包含"第"和"章"，或者数字开头
                if (trimmed.match(/第.*章/) || trimmed.match(/^\d+\./)) {
                    chapters.push(trimmed);
                }
            }

            return createResponse(true, "章节列表获取成功", {
                chapters: chapters,
                total: chapters.length
            });
        } catch (e: any) {
            return createResponse(false, "浏览章节失败: " + e.message);
        }
    }

    /**
     * 提取章节内容（包括OCR）
     */
    async function extract_chapter_content(params: { chapter_name: string }): Promise<any> {
        try {
            const chapterName = params.chapter_name;
            console.log(`正在提取章节内容: ${chapterName}`);

            // 获取当前页面并查找章节
            const page = await UINode.getCurrentPage();
            const chapterElement = page.findByText(chapterName);

            if (!chapterElement) {
                return createResponse(false, "未找到章节: " + chapterName);
            }

            // 点击章节
            await chapterElement.click();
            await Tools.System.sleep(2000);

            // 获取页面文本内容
            const updatedPage = await UINode.getCurrentPage();
            let textContent = updatedPage.getText();

            // 截图并OCR识别
            const screenshotResult = await Tools.UI.captureScreen();
            if (screenshotResult.success && screenshotResult.image_path) {
                const ocrResult = await Tools.Vision.ocr({
                    image_path: screenshotResult.image_path
                });

                if (ocrResult.success && ocrResult.text) {
                    textContent += "\n\n[OCR识别内容]\n" + ocrResult.text;
                }
            }

            return createResponse(true, "内容提取成功", {
                chapter: chapterName,
                content: textContent,
                textLength: textContent.length
            });
        } catch (e: any) {
            return createResponse(false, "提取内容失败: " + e.message);
        }
    }

    /**
     * 保存到记忆库
     */
    async function save_to_memory(params: { course_name: string; chapter_name: string; content: string }): Promise<any> {
        try {
            const { course_name, chapter_name, content } = params;
            console.log(`正在保存到记忆库: ${course_name} - ${chapter_name}`);

            // 构建记忆标题
            const memoryTitle = `[${course_name}] ${chapter_name}`;

            // 调用记忆库创建API
            const result = await Tools.Memory.create({
                title: memoryTitle,
                content: content
            });

            if (result.success) {
                return createResponse(true, "已保存到记忆库", {
                    memoryTitle: memoryTitle
                });
            } else {
                return createResponse(false, "保存失败: " + (result.error || "未知错误"));
            }
        } catch (e: any) {
            return createResponse(false, "保存到记忆库失败: " + e.message);
        }
    }

    /**
     * 返回首页
     */
    async function navigate_to_home(): Promise<any> {
        try {
            console.log("正在返回首页...");

            // 多次返回到首页
            for (let i = 0; i < 3; i++) {
                await Tools.UI.pressKey("KEYCODE_BACK");
                await Tools.System.sleep(500);
            }

            return createResponse(true, "已返回首页");
        } catch (e: any) {
            return createResponse(false, "返回首页失败: " + e.message);
        }
    }

    // 导出工具函数
    return {
        workflow_guide: function () {
            return {
                success: true,
                guide: "学习通智能助手使用指南",
                steps: [
                    "1. 打开学习通: open_xuexitong()",
                    "2. 选择课程: select_course({ course_name: '软件工程' })",
                    "3. 浏览章节: browse_chapters()",
                    "4. 提取内容: extract_chapter_content({ chapter_name: '第1章 概述' })",
                    "5. 保存记忆: save_to_memory({ course_name, chapter_name, content })",
                    "6. 之后直接问AI问题，它会从记忆库中检索回答"
                ]
            };
        },
        open_xuexitong: open_xuexitong,
        select_course: select_course,
        browse_chapters: browse_chapters,
        extract_chapter_content: extract_chapter_content,
        save_to_memory: save_to_memory,
        navigate_to_home: navigate_to_home
    };
})();

// 注册工具到全局
if (typeof global !== 'undefined') {
    global.XuexitongAssistant = XuexitongAssistant;
}
