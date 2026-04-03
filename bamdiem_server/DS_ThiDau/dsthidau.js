const host = config;
        let selectedIds = [];
        let isUpdateMode = false;

        const dialogview = ()=>{
            var dialogView = document.getElementById("dialog-view");

            var openView = document.getElementById("openDiaLogAdd");

            var closeView = document.querySelector("#dialog-view .close");

            openView.onclick = function() {
                dialogView.style.display = "block";
            }

            closeView.onclick = function() {
                dialogView.style.display = "none";
            }

            window.onclick = function(event) {
                if (event.target == dialogView) {
                    dialogView.style.display = "none";
                }
            }
            dialogView.addEventListener('click', (event) => {
                event.stopPropagation();
            });

        };
        dialogview();

        document.getElementById('openDiaLogAdd').addEventListener('click', () => {
            isUpdateMode = false;
            document.getElementById('name_n1').value = '';
            document.getElementById('province_n1').value = 'Đơn vị (Red)';
            document.getElementById('name_n2').value = '';
            document.getElementById('province_n2').value = 'Đơn vị (Blue)';
            document.getElementById('sex').value = 'Chọn giới tính';
            document.getElementById('type').value = 'Chọn loại';
            document.getElementById('group').value = 'Chọn nhóm';
            document.getElementById('weight').value = 'Chọn hạng cân';
            document.getElementById("dialog-title").textContent = "Add";
            document.getElementById("dialog-view").style.display = "block";
});
       
        const themRound = () =>{

        document.getElementById('addDSTD').addEventListener('submit', async(event)=>{
            event.preventDefault();

            const id = document.getElementById('record_id').value;
            const nameN1 = document.getElementById('name_n1').value;
            const provinceN1 = document.getElementById('province_n1').value;
            const nameN2 = document.getElementById('name_n2').value;
            const provinceN2 = document.getElementById('province_n2').value;
            const sex = document.getElementById('sex').value;
            const type = document.getElementById('type').value;
            const group = document.getElementById('group').value;
            const weight = document.getElementById('weight').value;

            const data = {
                round: 'Chờ',
                name_n1: nameN1,
                province_n1: provinceN1,
                diem_n1: 0,
                name_n2: nameN2,
                province_n2: provinceN2,
                diem_n2: 0,
                minute: 0,
                second: 0,
                weight: weight,
                group: group,
                sex: sex,
                type: type,
                bdiem: null,
            };
            try{
                let response;
                if (id) {
                    response = await fetch(`http://${config.host}:${config.port}/api/up_thidau/${id}`, {
                        method: 'PUT',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(data),
                    });
                } else {
                    response = await fetch(`http://${config.host}:${config.port}/api/add_thidau`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(data),
                    });
                }

                if (!response.ok) {
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }
                const kq = await response.json();
                alert('Thành công !');
                hienThi();
                
            } catch(error){
                console.error('Lỗi khi thêm vị trí mới:', error);
            }

        });
    }
        themRound();

        const hienThi = async () => {
            const tbody = document.querySelector('#tbody');

            try {
                const api = await fetch(`http://${config.host}:${config.port}/api/list_thidau`);
                if (!api.ok) {
                    throw new Error(`HTTP error! Status: ${api.status}`);
                }
                const data = await api.json();
                // console.log(data);

                // Xóa dữ liệu cũ
                tbody.innerHTML = '';

                // Hiển thị dữ liệu mới
                data.forEach((item, index) => {
                    const tr = document.createElement('tr');

                    const tdStt = document.createElement('td');
                    tdStt.textContent = index+1;

                    const tdName1 = document.createElement('td');
                    tdName1.textContent = item.name_n1;

                    const tdPro_n1 = document.createElement('td');
                    tdPro_n1.textContent = item.province_n1;

                    const tdName2 = document.createElement('td');
                    tdName2.textContent = item.name_n2;

                    const tdPro_n2 = document.createElement('td');
                    tdPro_n2.textContent = item.province_n2;

                    const tdWeight = document.createElement('td');
                    tdWeight.textContent = item.weight + " - " +item.sex+ " - " +item.type+ " - " +item.group;

                    const tdKQ = document.createElement('td');

                    // Tạo một span cho item.diem_n1 và thiết lập màu chữ đỏ
                    const diemN1Element = document.createElement('span');
                    diemN1Element.textContent = item.diem_n1;
                    diemN1Element.style.color = 'red'; // Màu chữ đỏ

                    // Tạo một span cho item.diem_n2 và thiết lập màu chữ xanh
                    const diemN2Element = document.createElement('span');
                    diemN2Element.textContent = item.diem_n2;
                    diemN2Element.style.color = 'blue'; // Màu chữ xanh

                    const tdTT = document.createElement('td');
                    tdTT.classList.add('tdTT');

                    const btnUpdate = document.createElement('button');
                    btnUpdate.textContent = 'Update';
                    btnUpdate.classList.add('btn', 'btn-outline-primary');
                    btnUpdate.addEventListener('click', async () => {
                        isUpdateMode = true;
                        document.getElementById('record_id').value = item._id;
                        document.getElementById('name_n1').value = item.name_n1;
                        document.getElementById('province_n1').value = item.province_n1;
                        document.getElementById('name_n2').value = item.name_n2;
                        document.getElementById('province_n2').value = item.province_n2;
                        document.getElementById('sex').value = item.sex;
                        document.getElementById('type').value = item.type;
                        document.getElementById('group').value = item.group;
                        document.getElementById('weight').value = item.weight;
                        document.getElementById("dialog-title").textContent = "Update";
                        document.getElementById("dialog-view").style.display = "block";
                    });
                    document.getElementById("dialog-close-button").addEventListener('click', () => {
                        if (isUpdateMode) {
                            location.reload();
                        }
                    });

                    const btnDel = document.createElement('button');
                    btnDel.textContent = 'Remove';
                    btnDel.classList.add('btn', 'btn-outline-danger');
                    btnDel.addEventListener('click', async ()=>{
                        try {
                            const conf = confirm(`Do you want to delete this position ${item.vitri} ?`);
                            if (conf) {
                                const response = await fetch(`http://${config.host}:${config.port}/api/del_thidau/${item._id}`, {
                                method: 'DELETE'
                            });

                            if (!response.ok) {
                                throw new Error(`HTTP error! Status: ${response.status}`);
                            }
                            // Xóa hàng từ DOM
                            tr.remove();
                            alert('Xóa thành công');
                            hienThi();
                            }
                    
                        } catch (error) {
                            console.error('Lỗi khi xóa vị trí:', error);
                        }
                    });
                    
                    tr.appendChild(tdStt);
                    tr.appendChild(tdName1);
                    tr.appendChild(tdPro_n1);
                    tr.appendChild(tdName2);
                    tr.appendChild(tdPro_n2);
                    tr.appendChild(tdWeight);
                    tr.appendChild(tdKQ)
                    tr.appendChild(tdTT);

                    tdKQ.appendChild(diemN1Element);
                    tdKQ.appendChild(document.createTextNode(" - ")); // Thêm dấu gạch ngang
                    tdKQ.appendChild(diemN2Element);
                    tdTT.appendChild(btnUpdate);
                    tdTT.appendChild(btnDel);
                    // tdTT.appendChild(btnView);
                    // tdTT.appendChild(btnDiem);
            
                    tbody.appendChild(tr);
                });
            } catch (error) {
                console.error('Fetch error:', error);
            }
        }
        hienThi();
        setInterval(hienThi, 1000);

    
        document.getElementById('viewdiem').addEventListener('click', async () => {
            window.location.href = `../DS_ViTri/home.html`;
        });
        document.getElementById('viewlist').addEventListener('click', async () =>{
            window.location.href = `../View_ThiDau/view.html`;
        })
        

        document.getElementById('removeAll').addEventListener('click', async () => {
            try {
                const conf = confirm('Do you want to delete all records?');
                if (conf) {
                    const response = await fetch(`http://${config.host}:${config.port}/api/remove_all`, {
                        method: 'DELETE'
                    });

                    if (!response.ok) {
                        throw new Error(`HTTP error! Status: ${response.status}`);
                    }

                    alert('Đã xóa tất cả các bản ghi');
                    hienThi();
                }
            } catch (error) {
                console.error('Lỗi khi xóa tất cả các bản ghi:', error);
            }
        });

        document.addEventListener('DOMContentLoaded', function() {
            const sexSelect = document.getElementById('sex');
            const groupSelect = document.getElementById('group');
            const weightSelect = document.getElementById('weight');
            const typeSelect = document.getElementById('type');

            const weightOptions = {
                Nam: {
                    A: [
                        { value: 'Dưới 48kg', text: 'Dưới 48kg (Nhóm A - Giải VĐQG trẻ - Nam)' },
                        { value: '52kg', text: '52kg (trên 48kg đến 52kg) (Nhóm A - Giải VĐQG trẻ - Nam)' },
                        { value: '56kg', text: '56kg (trên 52kg đến 56kg) (Nhóm A - Giải VĐQG trẻ - Nam)' },
                        { value: '60kg', text: '60kg (trên 56kg đến 60kg) (Nhóm A - Giải VĐQG trẻ - Nam)' },
                        { value: '65kg', text: '65kg (trên 60kg đến 65kg) (Nhóm A - Giải VĐQG trẻ - Nam)' },
                        { value: '70kg', text: '70kg (trên 65kg đến 70kg) (Nhóm A - Giải VĐQG trẻ - Nam)' },
                        { value: '75kg', text: '75kg (trên 70kg đến 75kg) (Nhóm A - Giải VĐQG trẻ - Nam)' },
                        { value: '80kg', text: '80kg (trên 75kg đến 80kg) (Nhóm A - Giải VĐQG trẻ - Nam)' },
                    ],
                    B: [
                        { value: 'Dưới 48kg', text: 'Dưới 48kg (Nhóm B - Giải VĐQG trẻ - Nam)' },
                        { value: '52kg', text: '52kg (trên 48kg đến 52kg) (Nhóm B - Giải VĐQG trẻ - Nam)' },
                        { value: '56kg', text: '56kg (trên 52kg đến 56kg) (Nhóm B - Giải VĐQG trẻ - Nam)' },
                        { value: '60kg', text: '60kg (trên 56kg đến 60kg) (Nhóm B - Giải VĐQG trẻ - Nam)' },
                        { value: '65kg', text: '65kg (trên 60kg đến 65kg) (Nhóm B - Giải VĐQG trẻ - Nam)' },
                        { value: '70kg', text: '70kg (trên 65kg đến 70kg) (Nhóm B - Giải VĐQG trẻ - Nam)' },
                    ],
                    C: [
                        { value: 'Dưới 45kg', text: 'Dưới 45kg (Nhóm C - Giải VĐQG trẻ - Nam)' },
                        { value: '48kg', text: '48kg (trên 45kg đến 48kg) (Nhóm C - Giải VĐQG trẻ - Nam)' },
                        { value: '52kg', text: '52kg (trên 48kg đến 52kg) (Nhóm C - Giải VĐQG trẻ - Nam)' },
                        { value: '56kg', text: '56kg (trên 52kg đến 56kg) (Nhóm C - Giải VĐQG trẻ - Nam)' },
                        { value: '60kg', text: '60kg (trên 56kg đến 60kg) (Nhóm C - Giải VĐQG trẻ - Nam)' },
                    ],
                    GiaiLon: [
                        { value: 'Dưới 48kg', text: 'Dưới 48kg (Giải VĐQG lớn - Nam)' },
                        { value: '52kg', text: '52kg (trên 48kg đến 52kg) (Giải VĐQG lớn - Nam)' },
                        { value: '56kg', text: '56kg (trên 52kg đến 56kg) (Giải VĐQG lớn - Nam)' },
                        { value: '60kg', text: '60kg (trên 56kg đến 60kg) (Giải VĐQG lớn - Nam)' },
                        { value: '65kg', text: '65kg (trên 60kg đến 65kg) (Giải VĐQG lớn - Nam)' },
                        { value: '70kg', text: '70kg (trên 65kg đến 70kg) (Giải VĐQG lớn - Nam)' },
                        { value: '75kg', text: '75kg (trên 70kg đến 75kg) (Giải VĐQG lớn - Nam)' },
                        { value: '80kg', text: '80kg (trên 75kg đến 80kg) (Giải VĐQG lớn - Nam)' },
                        { value: '85kg', text: '85kg (trên 80kg đến 85kg) (Giải VĐQG lớn - Nam)' },
                        { value: '90kg', text: '90kg (trên 85kg đến 90kg) (Giải VĐQG lớn - Nam)' },
                        { value: 'Trên 90kg', text: 'Trên 90kg (Giải VĐQG lớn - Nam)' },
                    ]
                },
                Nữ: {
                    A: [
                        { value: 'Dưới 45kg', text: 'Dưới 45kg (Nhóm A - Giải VĐQG trẻ - Nữ)' },
                        { value: '48kg', text: '48kg (trên 45kg đến 48kg) (Nhóm A - Giải VĐQG trẻ - Nữ)' },
                        { value: '52kg', text: '52kg (trên 48kg đến 52kg) (Nhóm A - Giải VĐQG trẻ - Nữ)' },
                        { value: '56kg', text: '56kg (trên 52kg đến 56kg) (Nhóm A - Giải VĐQG trẻ - Nữ)' },
                        { value: '60kg', text: '60kg (trên 56kg đến 60kg) (Nhóm A - Giải VĐQG trẻ - Nữ)' },
                        { value: '65kg', text: '65kg (trên 60kg đến 65kg) (Nhóm A - Giải VĐQG trẻ - Nữ)' },
                        { value: '70kg', text: '70kg (trên 65kg đến 70kg) (Nhóm A - Giải VĐQG trẻ - Nữ)' },
                    ],
                    B: [
                        { value: 'Dưới 45kg', text: 'Dưới 45kg (Nhóm B - Giải VĐQG trẻ - Nữ)' },
                        { value: '48kg', text: '48kg (trên 45kg đến 48kg) (Nhóm B - Giải VĐQG trẻ - Nữ)' },
                        { value: '52kg', text: '52kg (trên 48kg đến 52kg) (Nhóm B - Giải VĐQG trẻ - Nữ)' },
                        { value: '56kg', text: '56kg (trên 52kg đến 56kg) (Nhóm B - Giải VĐQG trẻ - Nữ)' },
                        { value: '60kg', text: '60kg (trên 56kg đến 60kg) (Nhóm B - Giải VĐQG trẻ - Nữ)' },
                        { value: '65kg', text: '65kg (trên 60kg đến 65kg) (Nhóm B - Giải VĐQG trẻ - Nữ)' },
                    ],
                    C: [
                        { value: 'Dưới 42kg', text: 'Dưới 42kg (Nhóm C - Giải VĐQG trẻ - Nữ)' },
                        { value: '45kg', text: '45kg (trên 42kg đến 45kg) (Nhóm C - Giải VĐQG trẻ - Nữ)' },
                        { value: '48kg', text: '48kg (trên 45kg đến 48kg) (Nhóm C - Giải VĐQG trẻ - Nữ)' },
                        { value: '52kg', text: '52kg (trên 48kg đến 52kg) (Nhóm C - Giải VĐQG trẻ - Nữ)' },
                        { value: '56kg', text: '56kg (trên 52kg đến 56kg) (Nhóm C - Giải VĐQG trẻ - Nữ)' },
                    ],
                    GiaiLon: [
                        { value: 'Dưới 45kg', text: 'Dưới 45kg (Giải VĐQG lớn - Nữ)' },
                        { value: '48kg', text: '48kg (trên 45kg đến 48kg) (Giải VĐQG lớn - Nữ)' },
                        { value: '52kg', text: '52kg (trên 48kg đến 52kg) (Giải VĐQG lớn - Nữ)' },
                        { value: '56kg', text: '56kg (trên 48kg đến 56kg) (Giải VĐQG lớn - Nữ)' },
                        { value: '60kg', text: '60kg (trên 56kg đến 60kg) (Giải VĐQG lớn - Nữ)' },
                        { value: '65kg', text: '65kg (trên 60kg đến 65kg) (Giải VĐQG lớn - Nữ)' },
                        { value: '70kg', text: '70kg (trên 65kg đến 70kg) (Giải VĐQG lớn - Nữ)' },
                        { value: '75kg', text: '75kg (trên 70kg đến 75  kg) (Giải VĐQG lớn - Nữ)' },
                    ]
                }
            };

            function updateWeightOptions() {
                const selectedSex = sexSelect.value;
                const selectedGroup = groupSelect.value;
                const selectedType = typeSelect.value;
        
                weightSelect.innerHTML = '';
        
                if (selectedSex && selectedType) {
                    let options;
        
                    if (selectedType === 'Giải VĐ lớn' && weightOptions[selectedSex].GiaiLon) {
                        options = weightOptions[selectedSex].GiaiLon;
                    } else if (selectedGroup && weightOptions[selectedSex][selectedGroup]) {
                        options = weightOptions[selectedSex][selectedGroup];
                    }
        
                    if (options) {
                        options.forEach(option => {
                            const opt = document.createElement('option');
                            opt.value = option.value;
                            opt.textContent = option.text;
                            weightSelect.appendChild(opt);
                        });
                    } else {
                        const opt = document.createElement('option');
                        opt.value = '';
                        opt.textContent = 'Không có tùy chọn hạng cân';
                        weightSelect.appendChild(opt);
                    }
                }
            };
        
            const hideGroupField = () => {
                groupSelect.style.display = 'none';
                groupSelect.disabled = true;
            };
        
            const showGroupField = () => {
                groupSelect.style.display = 'block';
                groupSelect.disabled = false;
            };
        
        
            typeSelect.addEventListener('change', function() {
                const selectedType = typeSelect.value;
        
                if (selectedType === 'Giải VĐ lớn') {
                    hideGroupField();
                } else {
                    showGroupField();
                }
        
                updateWeightOptions();
            });
        
            sexSelect.addEventListener('change', updateWeightOptions);
            groupSelect.addEventListener('change', updateWeightOptions);
        });

        document.getElementById('btn_apk').addEventListener('click', function() {
            // Liên kết tải xuống trực tiếp từ Google Drive
            window.location.href = "https://drive.usercontent.google.com/download?id=1Fk56Ms7fg65N0Xat6hy6qhl4EeveXquL&export=download&authuser=0";
          });

    