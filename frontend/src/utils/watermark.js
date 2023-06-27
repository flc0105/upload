let watermark = {}; // 创建水印对象

let setWatermark = (text) => {
  let id = "watermark-" + Date.now(); // 生成唯一的水印 ID，使用当前时间的毫秒数作为 ID

  let existingWatermark = document.getElementById(id); // 检查是否已存在具有相同 ID 的水印
  if (existingWatermark !== null) {
    existingWatermark.parentNode.removeChild(existingWatermark); // 如果存在，则删除已存在的水印元素
  }

  let can = document.createElement("canvas"); // 创建画布元素
  //   can.width = 300;
  //   can.height = 240;

  can.width = 500; // 设置水印之间的左右间距
  can.height = 240; // 设置水印之间的上下间距

  const cans = can.getContext("2d"); // 获取画布的 2D 上下文
  if (cans) {
    cans.rotate((-20 * Math.PI) / 120); // 旋转水印角度
    cans.font = "15px Vedana"; // 设置水印字体
    cans.fillStyle = "rgba(0, 0, 0, 0.15)"; // 设置水印颜色
    cans.textAlign = "left"; // 设置水印文本对齐方式
    cans.textBaseline = "middle"; // 设置水印文本基线
    cans.fillText(text, can.width / 20, can.height); // 在画布上绘制水印文本
  }

  let water_div = document.createElement("div"); // 创建水印容器元素
  water_div.id = id; // 设置水印容器的 ID
  water_div.className = "watermarkClass"; // 设置水印容器的类名
  water_div.style.pointerEvents = "none"; // 禁止水印容器接收事件
  water_div.style.background =
    "url(" + can.toDataURL("image/png") + ") left top repeat"; // 使用画布生成的水印图像作为背景
  water_div.style.top = "0px"; // 设置水印容器相对于视口顶部的偏移量
  water_div.style.left = "0px"; // 设置水印容器相对于视口左侧的偏移量
  water_div.style.position = "fixed"; // 将水印容器定位为固定定位，使其固定在视口上
  water_div.style.zIndex = "100000"; // 设置水印容器的堆叠顺序
  water_div.style.width = "100%"; // 设置水印容器的宽度为100%
  water_div.style.height = "100%"; // 设置水印容器的高度为100%
  document.body.appendChild(water_div); // 将水印容器添加到文档的<body>元素中

  return id; // 返回水印的 ID
};

watermark.set = (text) => {
  const domArr = Array.from(document.getElementsByClassName("watermarkClass")); // 获取具有水印类名的元素集合
  domArr.forEach((element) => {
    element.remove(); // 移除所有具有水印类名的元素
  });

  if (text === "") {
    return; // 如果文本为空，则不添加水印
  }
  setWatermark(text); // 设置水印
};

export default watermark; // 导出水印对象
