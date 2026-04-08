const express = require('express');
const http = require('http');
const WebSocket = require('ws');
const bodyParser = require('body-parser');
const cors = require('cors');
const mongoose = require('mongoose');

const COMMON = require('./database/COMMON');
const bdiemModel = require('./database/bdiemModel');
const thidauModel = require('./database/thidauModel');

const app = express();
const port = 3000;

const server = http.createServer(app);
const wss = new WebSocket.Server({ server });

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
app.use(cors());

const router = express.Router();
app.use('/api', router);

const uri = COMMON.uri;

mongoose.connect(uri, {
    maxPoolSize: 10
}).then(() => {
    console.log('Kết nối MongoDB thành công');
}).catch(err => {
    console.error('Lỗi kết nối MongoDB:', err);
});

let currentData = {
    round: 'Chưa bắt đầu',
    name_n1: 'Giáp đỏ',
    province_n1: 'Đơn vị',
    name_n2: 'Giáp xanh',
    province_n2: 'Đơn vị',
    minute: 0,
    second: 0,
    diem_n1: 0,
    diem_n2: 0,
    timeLeft: -1
};

let isOn = false;
let isPlay = false;
let messageBuffer = [];
const MAX_BUFFER_SIZE = 50;

const debounce = (func, wait) => {
    let timeout;
    return (...args) => {
        clearTimeout(timeout);
        timeout = setTimeout(() => func(...args), wait);
    };
};

const broadcastWithTime = debounce((data) => {
    const payload = { ...data, timestamp: Date.now() };
    messageBuffer.push(payload);
    if (messageBuffer.length > MAX_BUFFER_SIZE) {
        messageBuffer.shift();
    }
    wss.clients.forEach(client => {
        if (client.readyState === WebSocket.OPEN) {
            client.send(JSON.stringify(payload));
        }
    });
}, 50);

wss.on('connection', (ws) => {
    ws.isAlive = true;
    ws.send(JSON.stringify(currentData));

    messageBuffer.forEach(msg => {
        if (ws.readyState === WebSocket.OPEN) {
            ws.send(JSON.stringify(msg));
        }
    });

    ws.on('message', (message) => {
        try {
            const data = JSON.parse(message);
            if (data.action === 'on') {
                isOn = true;
                broadcastWithTime({ action: 'statusUpdate', isOn });
            } else if (data.action === 'off') {
                isOn = false;
                broadcastWithTime({ action: 'statusUpdate', isOn });
            } else if (data.action === 'resetAll') {
                broadcastWithTime({ action: 'resetAll' });
            } else if (data.action === 'soundControl') {
                const soundAction = data.soundAction;
                broadcastWithTime({ action: 'soundControl', soundAction });
                if (soundAction === 'timerFinished') {
                    currentData.timeLeft = 0; // Cập nhật trạng thái timer
                    broadcastWithTime({ action: 'syncTimer', timeLeft: 0 }); // Gửi đồng bộ hóa
                    console.log('Broadcasted syncTimer with timeLeft: 0');
                }
            }
        } catch (error) {
            console.error('Lỗi xử lý message WebSocket:', error);
        }
    });

    ws.on('pong', () => ws.isAlive = true);
    ws.on('close', () => {});
    ws.on('error', (error) => console.error('WebSocket error:', error));
});

setInterval(() => {
    wss.clients.forEach(ws => {
        if (!ws.isAlive) return ws.terminate();
        ws.isAlive = false;
        ws.ping();
    });
}, 10000);
 
router.post('/view', (req, res) => {
    currentData = {
        round: req.body.round || currentData.round,
        name_n1: req.body.name_n1 || currentData.name_n1,
        province_n1: req.body.province_n1 || currentData.province_n1,
        name_n2: req.body.name_n2 || currentData.name_n2,
        province_n2: req.body.province_n2 || currentData.province_n2,
        minute: req.body.minute !== undefined ? req.body.minute : currentData.minute,
        second: req.body.second !== undefined ? req.body.second : currentData.second,
        diem_n1: req.body.diem_n1 !== undefined ? req.body.diem_n1 : currentData.diem_n1,
        diem_n2: req.body.diem_n2 !== undefined ? req.body.diem_n2 : currentData.diem_n2
    };

    wss.clients.forEach(client => {
        if (client.readyState === WebSocket.OPEN) {
            client.send(JSON.stringify(currentData));
        }
    });

    res.status(200).json({ message: 'Dữ liệu đã được cập nhật thành công', currentData });
});

router.put('/update-view/:id', (req, res) => {
    const id = req.params.id;
    const updatedData = {
        round: req.body.round || currentData.round,
        name_n1: req.body.name_n1 || currentData.name_n1,
        province_n1: req.body.province_n1 || currentData.province_n1,
        name_n2: req.body.name_n2 || currentData.name_n2,
        province_n2: req.body.province_n2 || currentData.province_n2,
        minute: req.body.minute !== undefined ? req.body.minute : currentData.minute,
        second: req.body.second !== undefined ? req.body.second : currentData.second,
        diem_n1: req.body.diem_n1 !== undefined ? req.body.diem_n1 : currentData.diem_n1,
        diem_n2: req.body.diem_n2 !== undefined ? req.body.diem_n2 : currentData.diem_n2
    };

    currentData = updatedData;

    wss.clients.forEach(client => {
        if (client.readyState === WebSocket.OPEN) {
            client.send(JSON.stringify(updatedData));
        }
    });

    res.status(200).json({ message: 'Dữ liệu đã được cập nhật thành công', data: updatedData });
});


app.get('/ds_bdiem', async (req, res) => {
    let bdiems = await bdiemModel.find();
    // console.log(bdiems);
    res.send(bdiems);
});

app.get('/ds_thidau', async (req, res) => {
    // await mongoose.connect(uri);
    let data = await thidauModel.find();
    // console.log(data);
    res.send(data);
});

router.get('/list', async (req, res) => {
    // await mongoose.connect(uri);
    let bdiems = await bdiemModel.find();
    res.send(bdiems);
});

// Thay vì chỉ đếm, giờ trả danh sách vị trí
router.get('/vitri', async (req, res) => {
    try {
        const data = await bdiemModel.find({}, 'vitri'); // chỉ lấy trường vitri
        res.json(data);
    } catch (err) {
        res.status(500).json({ error: 'Lỗi khi lấy dữ liệu' });
    }
});


router.post('/add', async (req, res) => {
    let bdiem = req.body;
    try {
        let kq = await bdiemModel.create(bdiem);
        // console.log('Vị trí mới đã được thêm:', kq);
        let bdiems = await bdiemModel.find();
        res.send(bdiems);
    } catch (error) {
        console.error('Lỗi khi thêm dữ liệu:', error);
        res.status(500).send('Lỗi khi thêm dữ liệu');
    }
});

router.put('/updatedo/:id', async (req, res) => {
    try {
        const id = req.params.id;
        const { diemdo } = req.body;
        // await mongoose.connect(uri);
        let bdiem = await bdiemModel.findById(id);
        if (!bdiem) {
            return res.status(404).json({ message: 'Không tìm thấy bản ghi' });
        }
        const diemTangThem = diemdo - bdiem.diemdo;
        const historyEntry = `${diemTangThem}`;
        bdiem.lichsudo.push(historyEntry);
        bdiem.diemdo = diemdo;
        await bdiem.save();
        const updateMessage = {
            action: 'updateScores',
            vitri: bdiem.vitri,
            diemdo: bdiem.diemdo,
            diemxanh: bdiem.diemxanh
        };        
        wss.clients.forEach(client => {
            if (client.readyState === WebSocket.OPEN) {
                client.send(JSON.stringify(updateMessage));
            }
        });
        res.json({ message: 'Cập nhật điểm thành công và lưu lịch sử.', bdiem });
    } catch (error) {
        console.error('Lỗi khi cập nhật:', error);
        res.send('Lỗi khi cập nhật');
    }
});

router.put('/updatexanh/:id', async (req, res) => {
    try {
        const id = req.params.id;
        const { diemxanh } = req.body;
        // await mongoose.connect(uri);
        let bdiem = await bdiemModel.findById(id);
        if (!bdiem) {
            return res.status(404).json({ message: 'Không tìm thấy bản ghi' });
        }
        const diemTangThem = diemxanh - bdiem.diemxanh;
        const historyEntry = `${diemTangThem}`;
        bdiem.lichsuxanh.push(historyEntry);
        bdiem.diemxanh = diemxanh;
        await bdiem.save(); 
        const updateMessage = {
            action: 'updateScores',
            vitri: bdiem.vitri,
            diemdo: bdiem.diemdo,
            diemxanh: bdiem.diemxanh
        };        
        wss.clients.forEach(client => {
            if (client.readyState === WebSocket.OPEN) {
                client.send(JSON.stringify(updateMessage));
            }
        });
        res.json({ message: 'Cập nhật điểm thành công và lưu lịch sử.', bdiem });
    } catch (error) {
        console.error('Lỗi khi cập nhật:', error);
        res.send('Lỗi khi cập nhật');
    }
});

router.delete('/xoa/:id', async (req, res) => {
    try {
        // await mongoose.connect(uri);
        let id = req.params.id;
        // console.log(id);
        const result = await bdiemModel.deleteOne({ _id: id });
        if (result) {
            // console.log('Xoa thanh cong');
        } else {
            res.send('Xóa không thành công');
        }
    } catch (error) {
        console.error('Lỗi khi xóa:', error);
        res.send('Lỗi khi xóa');
    }
});
router.put('/reset-all', async (req, res) => {
    try {
        // await mongoose.connect(uri);
        const result = await bdiemModel.updateMany({}, {
            diemdo: 0,
            diemxanh: 0,
            lichsudo: [],
            lichsuxanh: []
        });

        if (result) {
            res.send('Reset tất cả các điểm thành công');
            
            // Phát đi tín hiệu WebSocket tới Android để tải lại dữ liệu
            const resetMessage = { action: 'resetAll' };
            wss.clients.forEach(client => {
                if (client.readyState === WebSocket.OPEN) {
                    client.send(JSON.stringify(resetMessage));
                }
            });

        } else {
            res.status(500).send('Không tìm thấy dữ liệu để reset');
        }
    } catch (error) {
        console.error('Lỗi khi reset:', error);
        res.status(500).send('Lỗi khi reset');
    }
});

router.put('/reset/:id', async (req, res) => {
    try {
        const id = req.params.id;
        const data = {
            diemdo: 0,
            diemxanh: 0,
            lichsudo: [],
            lichsuxanh: [],
        };
        // await mongoose.connect(uri);
        const result = await bdiemModel.findByIdAndUpdate(id, data);
        if (result) {
            res.send('Reset tất cả các điểm thành công');
            const resetMessage = { action: 'resetAll' };
            wss.clients.forEach(client => {
                if (client.readyState === WebSocket.OPEN) {
                    client.send(JSON.stringify(resetMessage));
                }
            });

        } else {
            res.status(500).send('Không tìm thấy dữ liệu để reset');
        }
    } catch (error) {
        console.error('Lỗi khi reset:', error);
        res.send('Lỗi khi reset');
    }
});

router.get('/list/:id', async (req, res) => {
    const id = req.params.id;
    try {
        const result = await bdiemModel.findById(id);
        if (result) {
            res.json(result);
        } else {
            res.status(404).send('Không tìm thấy dữ liệu');
        }
    } catch (error) {
        console.error('Lỗi khi lấy dữ liệu:', error);
        res.status(500).send('Lỗi khi lấy dữ liệu');
    }
});


router.get('/list_thidau', async (req, res) => {
    // await mongoose.connect(uri);
    let data = await thidauModel.find();
    // console.log(data);
    res.send(data);
});

router.post('/add_thidau', async (req, res) => {
    // await mongoose.connect(uri);
    let data = req.body;
    let kq = await thidauModel.create(data);
    let data2 = await thidauModel.find();
    // console.log(data2);
    res.send(data2);
});

router.delete('/del_thidau/:id', async (req, res) => {
    try {
        // await mongoose.connect(uri);
        let id = req.params.id;
        console.log(id);
        const result = await thidauModel.deleteOne({ _id: id });
        if (result) {
            console.log('Xoa thanh cong');
        } else {
            res.send('Xóa không thành công');
        }
    } catch (error) {
        console.error('Lỗi khi xóa:', error);
        res.send('Lỗi khi xóa');
    }
});

router.put('/up_thidau/:id', async (req, res) => {
    try {
        const id = req.params.id;
        const data = req.body;
        // await mongoose.connect(uri);
        const result = await thidauModel.findByIdAndUpdate(id, data);
        if (result) {
            let data = await thidauModel.find();
            // console.log(data);
            res.send(data);
        } else {
            res.send('Không tìm thấy sản phẩm để cập nhật');
        }
    } catch (error) {
        console.error('Lỗi khi cập nhật:', error);
        res.send('Lỗi khi cập nhật');
    }
});

router.delete('/remove_all', async (req, res) => {
    try {
        // await mongoose.connect(uri);
        const result = await thidauModel.deleteMany({});
        if (result.deletedCount > 0) {
            res.send({ message: 'Đã xóa tất cả bản ghi thành công' });
        } else {
            res.send({ message: 'Không có bản ghi nào để xóa' });
        }
    } catch (error) {
        console.error('Lỗi khi xóa tất cả bản ghi:', error);
        res.status(500).send({ message: 'Lỗi khi xóa tất cả bản ghi' });
    }
});

router.get('/list_thidau/:id', async (req, res) => {
    const id = req.params.id;
    try {
        const result = await thidauModel.findById(id);
        if (result) {
            res.json(result);
        } else {
            res.status(404).send('Không tìm thấy dữ liệu');
        }
    } catch (error) {
        console.error('Lỗi khi lấy dữ liệu:', error);
        res.status(500).send('Lỗi khi lấy dữ liệu');
    }
});

// Start the server
server.listen(port,"0.0.0.0", () => {
    console.log(`Server running on port ${port}`);
});